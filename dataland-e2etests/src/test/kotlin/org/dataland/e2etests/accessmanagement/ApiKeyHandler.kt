package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.openApiClient.model.RevokeApiKeyResponse
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.e2etests.BASE_PATH_TO_API_KEY_MANAGER
import org.dataland.e2etests.utils.UserType
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi(BASE_PATH_TO_API_KEY_MANAGER)

    private fun requestApiKeyAndMetaData(userType: UserType, daysValid: Int): ApiKeyAndMetaInfo {
        tokenHandler.obtainTokenForUserType(userType)
        return apiKeyManagerClient.generateApiKey(daysValid)
    }

    fun obtainApiKeyForUserType(userType: UserType, daysValid: Int): String {
        val apiKeyAndMetaInfo = requestApiKeyAndMetaData(userType, daysValid)
        ApiClient.Companion.apiKey["dataland-api-key"] = apiKeyAndMetaInfo.apiKey
        setBearerTokenToNull()
        return apiKeyAndMetaInfo.apiKey
    }

    private fun revokeApiKey(userType: UserType): RevokeApiKeyResponse {
        tokenHandler.obtainTokenForUserType(userType)

        return apiKeyManagerClient.revokeApiKey()
    }
    fun revokeApiKeyForUser(userType: UserType): String {
        val revokeApiKeyResponse = revokeApiKey(userType)
        setBearerTokenToNull()
        return revokeApiKeyResponse.revokementProcessMessage
    }
    private fun setBearerTokenToNull() {
        ApiClientBackend.Companion.accessToken = null
        ApiClient.Companion.accessToken = null
    }

    fun validateApiKeyValidationMessage(receivedApiKey: String): String {
        return apiKeyManagerClient.validateApiKey(receivedApiKey).validationMessage.toString()
    }
}
