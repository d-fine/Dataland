package org.dataland.datalandbackend.apikey

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandbackend.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Value

@Component
class ApiKeyAuthenticationManager(
        @Value("\${dataland.apikeymananger.base-url}") var apikeymanagerBaseUrl: String
        ): AuthenticationManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun authenticate(authentication: Authentication?): Authentication {
        val controller = ApiKeyControllerApi(basePath = apikeymanagerBaseUrl)
        val customToken = authentication!!.principal as String

        try {
            val isApiKeyValid = controller.validateApiKey(customToken)
        } catch (ex: Exception) {
           logger.info("Could not validate API-Key")
           throw ResourceNotFoundApiException("Could not validate API-Key","API-KEY Validation Service could not be queried",ex)
        }
        logger.info("AKAM: Token: $customToken")
        return PreAuthenticatedAuthenticationToken("AuthenticatedUser", "N/A", mutableListOf(GrantedAuthority { "ROLE_ADMIN" }, GrantedAuthority { "ROLE_USER" }))
    }
}
