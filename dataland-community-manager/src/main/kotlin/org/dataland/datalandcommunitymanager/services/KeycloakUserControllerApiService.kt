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

/**
 * Class to manage the clients interaction with keycloak,
 * e.g. retrieving user information like email address
 */
@Service("KeycloakUserControllerApiService")
class KeycloakUserControllerApiService(
    @Autowired private val objectMapper: ObjectMapper,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class User(
        @JsonProperty("email")
        val email: String?,

        @JsonProperty("id")
        val userId: String,

        @JsonProperty("firstName") // TODO check if this is right
        val firstName: String?,

        @JsonProperty("lastName") // TODO check if this right
        val lastName: String?,
    )

    /**
     * get user information for given keycloak user id
     * @param userId the userId of the user in question
     * @returns the User Object
     */
    fun getUser(userId: String): User {
        val request = Request.Builder()
            .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId")
            .build()
        val response = authenticatedOkHttpClient.newCall(request).execute()
        val parsedResponseBody = objectMapper.readValue(
            response.body!!.string(),
            User::class.java,
        )
        return parsedResponseBody
    }
}
