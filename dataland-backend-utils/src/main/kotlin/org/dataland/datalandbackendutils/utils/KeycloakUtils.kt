package org.dataland.datalandbackendutils.utils

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.model.KeycloakUserInfo

/**
 * Gets the email address of a keycloak user via http request.
 * @param okHttpClient must be authenticated to be authorized to fetch the user infos of keycloak users
 * @param objectMapper required to parse the response into a Kotlin object
 * @param keycloakBaseUrl required for the target url
 * @param userId required to set the path param of the target url to receive the desired user info from keycloak
 * @returns the email address
 */
fun getEmailAddress(
    okHttpClient: OkHttpClient,
    objectMapper: ObjectMapper,
    keycloakBaseUrl: String,
    userId: String,
): String {
    val request = Request.Builder()
        .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId")
        .build()
    val response = okHttpClient.newCall(request).execute()
    val parsedResponseBody = objectMapper.readValue(
        response.body!!.string(),
        KeycloakUserInfo::class.java,
    )
    return parsedResponseBody.email ?: ""
}
