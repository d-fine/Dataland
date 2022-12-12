package org.dataland.e2etests.accessmanagement

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.utils.BearerTokenParser
import org.dataland.e2etests.MUTUAL_ROLES_UPLOADER_READER
import org.dataland.e2etests.PATH_TO_KEYCLOAK_TOKENENDPOINT
import org.dataland.e2etests.READER_USER_ID
import org.dataland.e2etests.READER_USER_NAME
import org.dataland.e2etests.READER_USER_PASSWORD
import org.dataland.e2etests.TOKENREQUEST_CLIENT_ID
import org.dataland.e2etests.TOKENREQUEST_GRANT_TYPE
import org.dataland.e2etests.UPLOADER_EXTENDED_ROLES
import org.dataland.e2etests.UPLOADER_USER_ID
import org.dataland.e2etests.UPLOADER_USER_NAME
import org.dataland.e2etests.UPLOADER_USER_PASSWORD
import org.dataland.e2etests.utils.UserType
import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ApiClient as ApiClientApiKeyManager
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend

class TokenHandler {

    private val client = OkHttpClient()
    private val bearerTokenParser = BearerTokenParser()

    private fun buildTokenRequest(username: String, password: String): Request {
        val requestBody = FormBody.Builder()
            .add("grant_type", TOKENREQUEST_GRANT_TYPE)
            .add("client_id", TOKENREQUEST_CLIENT_ID)
            .add("username", username)
            .add("password", password)
            .build()
        return Request.Builder()
            .url(PATH_TO_KEYCLOAK_TOKENENDPOINT)
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()
    }

    private fun requestToken(username: String, password: String): String {
        val response = client.newCall(buildTokenRequest(username, password)).execute()
        if (!response.isSuccessful) throw IllegalArgumentException("Token request failed, response is: $response")
        val responseBodyAsJsonString = response.body.string()
        return bearerTokenParser.getSingleValueFromDecodedBearerToken("access_token", responseBodyAsJsonString)!!
    }

    fun obtainTokenForUserType(userType: UserType) {
        val token = when (userType) {
            UserType.Reader -> requestToken(READER_USER_NAME, READER_USER_PASSWORD)
            UserType.Uploader -> requestToken(UPLOADER_USER_NAME, UPLOADER_USER_PASSWORD)
        }
        ApiClientBackend.Companion.accessToken = token
        ApiClientApiKeyManager.Companion.accessToken = token
    }

    fun getCurrentToken(): String? {
        if (ApiClientBackend.Companion.accessToken != ApiClientApiKeyManager.Companion.accessToken) {
            throw IllegalArgumentException("Currently there is not one token set for all clients.")
        }
        return ApiClientBackend.Companion.accessToken
    }

    fun setTokensToNullForAllClients() {
        ApiClientBackend.Companion.accessToken = null
        ApiClientApiKeyManager.Companion.accessToken = null
    }

    fun getUserIdForTechnicalUsers(userType: UserType): String {
        return when (userType) {
            UserType.Reader -> READER_USER_ID
            UserType.Uploader -> UPLOADER_USER_ID
        }
    }
    fun getRolesForTechnicalUsers(userType: UserType): List<String> {
        return when (userType) {
            UserType.Reader -> MUTUAL_ROLES_UPLOADER_READER
            UserType.Uploader -> MUTUAL_ROLES_UPLOADER_READER + UPLOADER_EXTENDED_ROLES
        }
    }
}
