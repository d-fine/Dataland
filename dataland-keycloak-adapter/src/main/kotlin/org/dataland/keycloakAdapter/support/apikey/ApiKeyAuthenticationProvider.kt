package org.dataland.keycloakAdapter.support.apikey

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ClientException
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ServerException
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.dataland.datalandbackendutils.apikey.ApiKeyUtility
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.keycloakAdapter.auth.DatalandApiKeyAuthentication
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import java.io.IOException
import java.lang.IllegalStateException

/**
 * This provider supports login via API-Keys provided as bearer tokens.
 * Generates a DatalandApiKeyAuthentication upon successful validation of the Api-Key
 * via the token introspection endpoint
 */
class ApiKeyAuthenticationProvider(val apiKeyManagerBaseUrl: String) : AuthenticationProvider {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun authenticate(authentication: Authentication): DatalandApiKeyAuthentication? {
        val bearerToken = authentication as BearerTokenAuthenticationToken
        logger.trace("Received request for authentication with bearer token ${bearerToken.token}")

        try {
            ApiKeyUtility().parseApiKey(bearerToken.token)
        } catch (ex: InvalidInputApiException) {
            logger.trace("Could not parse API Key from input, deferring to next AuthenticationProvider", ex)
            return null
        }

        val apiKeyAuthentication = validateApiKeyAndGenerateAuthentication(bearerToken.token)
        apiKeyAuthentication.isAuthenticated = true

        return apiKeyAuthentication
    }

    private fun validateApiKeyAndGenerateAuthentication(apiKey: String): DatalandApiKeyAuthentication {
        val apiKeyMetaInfo = validateApiKeyViaEndpoint(apiKey)
        return DatalandApiKeyAuthentication(apiKey, apiKeyMetaInfo)
    }

    private fun validateApiKeyViaEndpoint(customToken: String): ApiKeyMetaInfo {
        var apiKeyMetaInfo = ApiKeyMetaInfo()
        try {
            logger.trace("Sending API-Token to API-Token-Service for introspection")
            apiKeyMetaInfo = ApiKeyControllerApi(basePath = apiKeyManagerBaseUrl).validateApiKey(customToken)
            logger.trace("Received API-Key Meta-Information {}", apiKeyMetaInfo)
            if (apiKeyMetaInfo.active == null || apiKeyMetaInfo.active == false) {
                logger.trace("API-Token came back invalid, throwing error to cancel authentication")
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

    override fun supports(authentication: Class<*>): Boolean {
        return BearerTokenAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}
