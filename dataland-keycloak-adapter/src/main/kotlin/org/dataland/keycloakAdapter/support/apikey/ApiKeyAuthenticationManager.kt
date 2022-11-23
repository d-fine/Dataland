package org.dataland.keycloakAdapter.support.apikey

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ApiKeyAuthenticationManager(
    @Value("\${dataland.apikeymananger.base-url}") var apikeymanagerBaseUrl: String
) : AuthenticationManager {
    override fun authenticate(authentication: Authentication?): Authentication {
        val controller = ApiKeyControllerApi(basePath = apikeymanagerBaseUrl)
        val customToken: String
        try {
            customToken = authentication!!.principal as String
        } catch (ex: Exception) {
            throw AuthenticationCredentialsNotFoundException("No Credentials found", ex)
        }

        val apiKeyMetaInfo: ApiKeyMetaInfo
        try {
            // TODO: Was passiert wenn es den API-Key nicht gibt?
            apiKeyMetaInfo = controller.validateApiKey(customToken)
        } catch (ex: Exception) {
            throw InternalAuthenticationServiceException("API-KEY Validation Service could not be queried", ex)
        }
        if (LocalDate.now().isAfter(apiKeyMetaInfo.expiryDate)) {
            throw CredentialsExpiredException("Token has expired")
        }
        return PreAuthenticatedAuthenticationToken(apiKeyMetaInfo.keycloakUserId, "N/A", apiKeyMetaInfo.keycloakRoles.map { GrantedAuthority { it } }.toList())
    }
}
