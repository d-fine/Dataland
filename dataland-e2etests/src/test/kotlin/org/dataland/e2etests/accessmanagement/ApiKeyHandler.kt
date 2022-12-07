package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.e2etests.BASE_PATH_TO_API_KEY_MANAGER
import org.dataland.e2etests.utils.UserType
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi(BASE_PATH_TO_API_KEY_MANAGER)

    fun obtainApiKeyForUserTypeAndRevokeBearerToken(userType: UserType, daysValid: Int): String {
        tokenHandler.obtainTokenForUserType(userType)
        val apiKeyAndMetaInfo = apiKeyManagerClient.generateApiKey(daysValid)
        ApiClientBackend.Companion.apiKey["dataland-api-key"] = apiKeyAndMetaInfo.apiKey
        tokenHandler.revokeTokenFromAllClients()
        return apiKeyAndMetaInfo.apiKey
    }

    fun revokeApiKeyForUserTypeAndRevokeBearerToken(userType: UserType): String {
        tokenHandler.obtainTokenForUserType(userType)
        val revokeApiKeyResponse = apiKeyManagerClient.revokeApiKey()
        tokenHandler.revokeTokenFromAllClients()
        return revokeApiKeyResponse.revokementProcessMessage
    }

    fun validateApiKeyAndReturnValidationMessage(receivedApiKey: String): String {
        return apiKeyManagerClient.validateApiKey(receivedApiKey).validationMessage!!
    }
}
