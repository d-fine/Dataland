package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.e2etests.BASE_PATH_TO_API_KEY_MANAGER
import org.dataland.e2etests.utils.UserType
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi(BASE_PATH_TO_API_KEY_MANAGER)

    private fun setBearerTokenToNull() {
        ApiClientBackend.Companion.accessToken = null
        ApiClient.Companion.accessToken = null
    }

    fun obtainApiKeyForUserTypeAndRevokeBearerToken(userType: UserType, daysValid: Int): String {
        tokenHandler.obtainTokenForUserType(userType)
        val apiKeyAndMetaInfo = apiKeyManagerClient.generateApiKey(daysValid)
        ApiClient.Companion.apiKey["dataland-api-key"] = apiKeyAndMetaInfo.apiKey
        setBearerTokenToNull()
        return apiKeyAndMetaInfo.apiKey
    }

    fun revokeApiKeyForUserTypeAndRevokeBearerToken(userType: UserType): String {
        tokenHandler.obtainTokenForUserType(userType)
        val revokeApiKeyResponse = apiKeyManagerClient.revokeApiKey()
        setBearerTokenToNull()
        return revokeApiKeyResponse.revokementProcessMessage
    }

    fun validateApiKeyAndReturnValidationMessage(receivedApiKey: String): String {
        return apiKeyManagerClient.validateApiKey(receivedApiKey).validationMessage!!
    }
}
