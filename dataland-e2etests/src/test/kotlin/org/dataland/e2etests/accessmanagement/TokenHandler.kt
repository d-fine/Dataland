package org.dataland.e2etests.accessmanagement

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.e2etests.ADMIN_USER_NAME
import org.dataland.e2etests.ADMIN_USER_PASSWORD
import org.dataland.e2etests.PATH_TO_KEYCLOAK_TOKENENDPOINT
import org.dataland.e2etests.SOME_USER_NAME
import org.dataland.e2etests.SOME_USER_PASSWORD
import org.dataland.e2etests.TOKENREQUEST_CLIENT_ID
import org.dataland.e2etests.TOKENREQUEST_GRANT_TYPE

class TokenHandler {

    private val objectMapper = ObjectMapper()
    private val client = OkHttpClient()

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
        val responseBodyAsString = response.body!!.string()
        val node: ObjectNode = objectMapper.readValue(responseBodyAsString, ObjectNode::class.java)
        return node.get("access_token").toString().trim('"')
    }

    fun obtainTokenForUserType(user: UserType) {
        ApiClient.Companion.accessToken = when (user) {
            UserType.SomeUser -> requestToken(SOME_USER_NAME, SOME_USER_PASSWORD)
            UserType.Admin -> requestToken(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
        }
    }

    enum class UserType {
        Admin,
        SomeUser
    }
}
