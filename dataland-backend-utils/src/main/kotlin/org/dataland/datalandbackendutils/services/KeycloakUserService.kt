package org.dataland.datalandbackendutils.services

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

/**
 * Class to manage the clients interaction with keycloak,
 * e.g. retrieving user information like email address
 */
@Service("KeycloakUserService")
@ConditionalOnProperty(name = ["dataland.keycloak.client-id"])
class KeycloakUserService(
    @Autowired private val objectMapper: ObjectMapper,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
    @Value("\${dataland.keycloak.base-url}") private val keycloakBaseUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun buildRequestToGetUserById(userId: String): Request =
        Request.Builder().url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId").build()

    /**
     * check if userId belongs to actual keycloak user
     * @param userId userId of the user in question
     * @return true if call to Keycloak API was successful, false otherwise
     */
    fun isKeycloakUserId(userId: String): Boolean {
        logger.info("Check if Keycloak userId '$userId' exists.")
        val request = buildRequestToGetUserById(userId)
        val response =
            authenticatedOkHttpClient
                .newCall(request)
                .execute()
        return response.isSuccessful
    }

    /**
     * get user information for given keycloak user id
     * if no user can be found, return KeycloakUserInfo object with input userId
     * @param userId the userId of the user in question
     * @returns the User Object
     */
    fun getUser(userId: String): KeycloakUserInfo {
        val request = buildRequestToGetUserById(userId)
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
     * get list of user information by keycloak role
     * @param role the keycloak role for which user information should be fetched
     * @returns the list of keycloak user info for the corresponding role
     */
    fun getUsersByRole(role: String): List<KeycloakUserInfo> {
        val completeUrl = "$keycloakBaseUrl/admin/realms/datalandsecurity/roles/$role/users/"

        val request =
            Request
                .Builder()
                .url(completeUrl)
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

    /**
     * Search keycloak users by email address or parts thereof
     * @param emailAddressSearchString the email address string to search for
     * @returns the list of keycloak user info matching the email search string
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
