package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKeyData
import org.dataland.e2etests.utils.UserType

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi()

    private fun requestApiKey(asUserType: UserType, daysValid: Int): ApiKeyData {

        tokenHandler.obtainTokenForUserType(asUserType)
        return apiKeyManagerClient.generateApiKey(daysValid)
    }

    fun obtainApiKeyForUserType(user: UserType, daysValid: Int) {
        val apiKeyData = requestApiKey(user, daysValid)
        // TODO put ApiKey value into the respective companion object =>
        // ApiClient.apiKey = apiKeyData.apiKey
    }
}
