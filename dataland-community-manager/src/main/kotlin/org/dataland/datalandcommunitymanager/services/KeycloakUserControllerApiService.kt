package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * get user information for given keycloak user id
     * @param userId the userId of the user in question
     * @returns the User Object
     */
    fun getUser(userId: String): KeycloakUserInfo {
        val request =
            Request
                .Builder()
                .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId")
                .build()
        val response =
            authenticatedOkHttpClient
                .newCall(request)
                .execute()
                .body!!
                .string()

        try {
            val user =
                objectMapper.readValue(
                    response,
                    KeycloakUserInfo::class.java,
                )
            return user
        } catch (e: JacksonException) {
            logger.warn("Failed to parse response from Keycloak. userId $userId. Response $response, exception: $e")
            return KeycloakUserInfo(email = null, userId = userId, firstName = null, lastName = null)
        }
    }

    /**
     * Search keycloak users by email address or parts thereof
     */
    fun searchUsers(emailAddressSearchString: String): List<KeycloakUserInfo> {
        val request =
            Request
                .Builder()
                .url("$keycloakBaseUrl/admin/realms/datalandsecurity/users?email=$emailAddressSearchString")
                .build()
        val response =
            authenticatedOkHttpClient
                .newCall(request)
                .execute()
                .body!!
                .string()

        try {
            val listOfUsers: List<KeycloakUserInfo> =
                objectMapper.readValue(
                    response,
                    object : TypeReference<List<KeycloakUserInfo>>() {},
                )
            return listOfUsers
        } catch (e: JacksonException) {
            logger.warn("Failed to parse response from Keycloak. Response $response, exception: $e")
            return emptyList()
        }
    }
}
