package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.openApiClient.model.RevokeApiKeyResponse
import org.dataland.datalandbackendutils.utils.BearerTokenParser
import org.dataland.e2etests.BASE_PATH_TO_API_KEY_MANAGER
import org.dataland.e2etests.utils.UserType
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi(BASE_PATH_TO_API_KEY_MANAGER)
    private val bearerTokenParser = BearerTokenParser()

    fun obtainApiKeyForUserType(userType: UserType, daysValid: Int? = null): ApiKeyAndMetaInfo {
        tokenHandler.obtainTokenForUserType(userType)
        val apiKeyAndMetaInfo = apiKeyManagerClient.generateApiKey(daysValid)
        ApiClientBackend.Companion.apiKey["dataland-api-key"] = apiKeyAndMetaInfo.apiKey
        return apiKeyAndMetaInfo
    }

    fun obtainApiKeyForUserTypeAndRevokeBearerTokens(userType: UserType, daysValid: Int? = null): ApiKeyAndMetaInfo {
        val apiKeyAndMetaInfo = obtainApiKeyForUserType(userType, daysValid)
        tokenHandler.setTokensToNullForAllClients()
        return apiKeyAndMetaInfo
    }

    fun revokeApiKeyForUserTypeAndRevokeBearerTokens(userType: UserType): RevokeApiKeyResponse {
        tokenHandler.obtainTokenForUserType(userType)
        val revokeApiKeyResponse = apiKeyManagerClient.revokeApiKey()
        tokenHandler.setTokensToNullForAllClients()
        return revokeApiKeyResponse
    }

    fun validateApiKeyAndReturnMetaInfo(receivedApiKey: String): ApiKeyMetaInfo {
        return apiKeyManagerClient.validateApiKey(receivedApiKey)
    }

    fun getApiKeyMetaInfoForUserType(userType: UserType): ApiKeyMetaInfo {
        tokenHandler.obtainTokenForUserType(userType)
        val currentToken = tokenHandler.getCurrentToken()
        val currentTokenPayloadDecoded = bearerTokenParser.decodeAndReturnBearerTokenPayload(currentToken!!)
        val keycloakUserId = bearerTokenParser.getKeycloakUserIdFromDecodedBearerToken(currentTokenPayloadDecoded)!!
        return apiKeyManagerClient.getApiKeyMetaInfoForUser(keycloakUserId)
    }

    fun deleteApiKeyFromBackendClient() {
        ApiClientBackend.Companion.apiKey.remove("dataland-api-key")
    }
}
