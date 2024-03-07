package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service("KeycloakUserControllerApiService")
class KeycloakUserControllerApiService(
    @Autowired private val objectMapper: ObjectMapper,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
    @Value("\${dataland.keycloak.client-id}") private val clientId: String,
    @Value("\${dataland.keycloak.client-secret}") private val clientSecret: String,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class User(
        @JsonProperty("email")
        val email: String,

        @JsonProperty("id")
        val userId: String,

    )

    /**
     * gets the email address of a user in keycloak given the user id
     * @param userId: the userId of the user in question
     * @returns the email address
     */
    @Transactional
    fun getEmailAddress(userId: String): String {
        val authorizationHeader = Base64.getEncoder().encodeToString(("$clientId:$clientSecret").toByteArray())
        val request = Request.Builder()
            .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", "Basic $authorizationHeader")
            .build()
        val response = authenticatedOkHttpClient.newCall(request).execute()
        val parsedResponseBody = objectMapper.readValue(
            response.body!!.string(),
            User::class.java,
        )
        return parsedResponseBody.email
    }
}
