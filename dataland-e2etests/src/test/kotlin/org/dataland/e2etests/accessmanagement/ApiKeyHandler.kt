package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyAndMetaInfo
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.e2etests.BASE_PATH_TO_API_KEY_MANAGER
import org.dataland.e2etests.utils.UserType

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi(BASE_PATH_TO_API_KEY_MANAGER)

    private fun requestApiKey(userType: UserType, daysValid: Int): ApiKeyAndMetaInfo {

        tokenHandler.obtainTokenForUserType(userType)
        return apiKeyManagerClient.generateApiKey(daysValid)
    }

    fun obtainApiKeyForUserType(userType: UserType, daysValid: Int) {
        val apiKeyData = requestApiKey(userType, daysValid)
        ApiClient.Companion.apiKey.put("dataland-api-key", apiKeyData.apiKey)
    }
}
