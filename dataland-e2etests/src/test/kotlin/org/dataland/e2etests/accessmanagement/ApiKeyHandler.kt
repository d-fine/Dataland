package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.openApiClient.model.RevokeApiKeyResponse
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.e2etests.BASE_PATH_TO_API_KEY_MANAGER
import org.dataland.e2etests.utils.UserType

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi(BASE_PATH_TO_API_KEY_MANAGER)

    private fun requestApiKeyAndMetaData(userType: UserType, daysValid: Int): ApiKeyAndMetaInfo {
        tokenHandler.obtainTokenForUserType(userType)
        return apiKeyManagerClient.generateApiKey(daysValid)
    }

    fun obtainApiKeyForUserType(userType: UserType, daysValid: Int) {
        val apiKeyAndMetaInfo = requestApiKeyAndMetaData(userType, daysValid)
        ApiClient.Companion.apiKey.put("dataland-api-key", apiKeyAndMetaInfo.apiKey)
    }

    fun revokeApiKeyForUser(userType: UserType): RevokeApiKeyResponse {
        tokenHandler.obtainTokenForUserType(userType)
        return apiKeyManagerClient.revokeApiKey()
    }
}
