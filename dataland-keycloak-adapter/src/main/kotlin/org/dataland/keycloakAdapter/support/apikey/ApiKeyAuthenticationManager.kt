package org.dataland.keycloakAdapter.support.apikey

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ClientException
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ServerException
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.dataland.datalandbackendutils.apikey.ApiKeyUtility
import org.dataland.datalandbackendutils.exceptions.ApiKeyFormatException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.IllegalStateException

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

        try {
            ApiKeyUtility().parseApiKey(customToken)
        } catch (ex: ApiKeyFormatException) {
            throw BadCredentialsException(ex.message, ex)
        }

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
        return PreAuthenticatedAuthenticationToken(
            apiKeyMetaInfo.keycloakUserId,
            "N/A",
            apiKeyMetaInfo.keycloakRoles!!.map { GrantedAuthority { it } }.toList()
        )
    }

    private fun validateApiKeyViaEndpoint(customToken: String): ApiKeyMetaInfo {
        var apiKeyMetaInfo = ApiKeyMetaInfo()
        try {
            apiKeyMetaInfo = ApiKeyControllerApi(basePath = apikeymanagerBaseUrl).validateApiKey(customToken)
            if (apiKeyMetaInfo.active == null || apiKeyMetaInfo.active == false) {
                throw BadCredentialsException(apiKeyMetaInfo.validationMessage)
            }
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
        return apiKeyMetaInfo
    }

    private fun handleAuthenticationException(ex: Exception) {
        val validationServiceCouldNotBeQueriedText = "API-KEY Validation Service could not be queried"
        throw InternalAuthenticationServiceException(validationServiceCouldNotBeQueriedText, ex)
    }
}
