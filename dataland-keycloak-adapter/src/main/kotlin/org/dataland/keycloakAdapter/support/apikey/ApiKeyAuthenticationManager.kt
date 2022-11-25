package org.dataland.keycloakAdapter.support.apikey

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ClientException
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ServerException
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.dataland.datalandbackendutils.apikey.ApiKeyPrevalidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.IllegalStateException
import java.time.LocalDate

/**
 * A class to validate an authentication and the corresponding token
 */
@ConditionalOnProperty("org.dataland.authorization.apikey.enable", havingValue = "true")
@Component
class ApiKeyAuthenticationManager(
    @Value("\${dataland.apikeymananger.base-url}") var apikeymanagerBaseUrl: String
) : AuthenticationManager {
    override fun authenticate(authentication: Authentication?): Authentication {
        val customToken = extractApiKey(authentication)

        // TDO: catch ApiKeyFormatException and process to whatever exceptions fits best
        ApiKeyPrevalidator().prevalidateApiKey(customToken)

        return validateApiKey(customToken)
    }

    private fun extractApiKey(authentication: Authentication?): String {
        val noCredentialsFoundText = "No Credentials found"
        try {
            if (authentication == null) {
                throw AuthenticationCredentialsNotFoundException(noCredentialsFoundText)
            }
            return authentication.principal as String
        } catch (ex: TypeCastException) {
            throw AuthenticationCredentialsNotFoundException(noCredentialsFoundText, ex)
        }
    }

    private fun validateApiKey(customToken: String): Authentication {
        val apiKeyMetaInfo: ApiKeyMetaInfo = validateApiKeyViaEndpoint(customToken)
        if (LocalDate.now().isAfter(apiKeyMetaInfo.expiryDate)) {
            throw CredentialsExpiredException("Token has expired")
        }
        return PreAuthenticatedAuthenticationToken(
            apiKeyMetaInfo.keycloakUserId,
            "N/A",
            apiKeyMetaInfo.keycloakRoles.map { GrantedAuthority { it } }.toList()
        )
    }

    private fun validateApiKeyViaEndpoint(customToken: String): ApiKeyMetaInfo {
        val controller = ApiKeyControllerApi(basePath = apikeymanagerBaseUrl)
        try {
            // TDO: Was passiert wenn es den API-Key nicht gibt?
            return controller.validateApiKey(customToken)
        } catch (ex: IllegalStateException) {
            handleAuthenticationException(ex)
        } catch (ex: IOException) {
            handleAuthenticationException(ex)
        } catch (ex: UnsupportedOperationException) {
            handleAuthenticationException(ex)
        } catch (ex: ClientException) {
            handleAuthenticationException(ex)
        } catch (ex: ServerException) {
            handleAuthenticationException(ex)
        }
        // TDO this is not reachable but something must be returned / thrown
        return ApiKeyMetaInfo("", listOf(""), LocalDate.now())
    }

    private fun handleAuthenticationException(ex: Exception) {
        val validationServiceCouldNotBeQueriedText = "API-KEY Validation Service could not be queried"
        throw InternalAuthenticationServiceException(validationServiceCouldNotBeQueriedText, ex)
    }
}
