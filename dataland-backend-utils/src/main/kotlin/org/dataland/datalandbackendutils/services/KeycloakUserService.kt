package org.dataland.datalandbackendutils.services

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.model.KeycloakMappingsRepresentation
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

    /**
     * check if userId belongs to actual keycloak user
     * @param userId userId of the user in question
     * @return true if call to Keycloak API was successful, false otherwise
     */
    fun isKeycloakUserId(userId: String): Boolean {
        logger.info("Check if Keycloak userId '$userId' exists.")
        val request = Request.Builder().url("$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId").build()
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
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId"
        val response = getKeycloakResponse(url)

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
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/roles/$role/users/"
        val response = getKeycloakResponse(url)
        return extractUsers(response)
    }

    /**
     * Search keycloak users by email address or parts thereof
     * @param emailAddressSearchString the email address string to search for
     * @returns the list of keycloak user info matching the email search string
     */
    fun searchUsers(emailAddressSearchString: String): List<KeycloakUserInfo> {
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/users?email=$emailAddressSearchString"
        val response = getKeycloakResponse(url)
        return extractUsers(response)
    }

    /**
     * Get keycloak roles for a user by their userId.
     */
    fun getUserRoleNames(userId: String): List<String> {
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId/role-mappings"
        val response = getKeycloakResponse(url)

        try {
            val mappingsRepresentation: KeycloakMappingsRepresentation =
                objectMapper.readValue(
                    response,
                    object : TypeReference<KeycloakMappingsRepresentation>() {},
                )
            return mappingsRepresentation.realmMappings.map {
                it.roleName
            }
        } catch (e: JacksonException) {
            logger.warn("Failed to parse response from Keycloak. Response $response, exception: $e")
            return emptyList()
        }
    }

    private fun getKeycloakResponse(url: String): String {
        val request = Request.Builder().url(url).build()
        return authenticatedOkHttpClient
            .newCall(request)
            .execute()
            .body!!
            .string()
    }

    private fun extractUsers(response: String): List<KeycloakUserInfo> {
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
