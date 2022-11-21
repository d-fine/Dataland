package org.dataland.datalandbackend.apikey

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)

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
        return PreAuthenticatedAuthenticationToken(apiKeyMetaInfo.username, "N/A", apiKeyMetaInfo.role.map { GrantedAuthority { it } }.toList())
    }
}
