package org.dataland.e2etests.accessmanagement

import org.dataland.datalandapikeymanager.openApiClient.api.ApiKeyControllerApi
import org.dataland.datalandapikeymanager.openApiClient.model.ApiKey
import org.dataland.e2etests.READER_USER_NAME
import org.dataland.e2etests.READER_USER_PASSWORD
import org.dataland.e2etests.UPLOADER_USER_NAME
import org.dataland.e2etests.UPLOADER_USER_PASSWORD
import org.dataland.e2etests.utils.UserType

class ApiKeyHandler {

    private val tokenHandler = TokenHandler()
    private val apiKeyManagerClient = ApiKeyControllerApi()

    private fun requestApiKey(username: String, password: String, daysValid: Long): ApiKey {
        // TODO ask Florian => Is there a reason why daysValid is a Long ?

        tokenHandler.obtainTokenForUserType(UserType.Reader)
        return apiKeyManagerClient.generateApiKey(daysValid)
    }

    fun obtainApiKeyForUserType(user: UserType, daysValid: Long) {
        val apiKey = when (user) {
            UserType.Reader -> requestApiKey(READER_USER_NAME, READER_USER_PASSWORD, daysValid)
            UserType.Uploader -> requestApiKey(UPLOADER_USER_NAME, UPLOADER_USER_PASSWORD, daysValid)
        }
        // TODO put username, expiryDate and ApiKey value into the respective companion object =>
        // ApiClientApiKeyManager.Companion.apiKey = mapOf<String, String>("todo" to "todo")
    }
}
