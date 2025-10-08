package org.dataland.datalandbackendutils.services

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.model.KeycloakMappingsRepresentation
import org.dataland.datalandbackendutils.model.KeycloakRoleRepresentation
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
     * @return the User Object
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
     * @return the list of keycloak user info for the corresponding role
     */
    fun getUsersByRole(role: String): List<KeycloakUserInfo> {
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/roles/$role/users/"
        val response = getKeycloakResponse(url)
        return extractUsers(response)
    }

    /**
     * Search keycloak users by email address or parts thereof
     * @param emailAddressSearchString the email address string to search for
     * @return the list of keycloak user info matching the email search string
     */
    fun searchUsers(emailAddressSearchString: String): List<KeycloakUserInfo> {
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/users?email=$emailAddressSearchString"
        val response = getKeycloakResponse(url)
        return extractUsers(response)
    }

    /**
     * Search keycloak users by email subdomain
     * @param emailSubdomain the email subdomain to search for
     * @return the list of keycloak user info matching the email subdomain
     */
    fun searchUsersByEmailSubdomain(emailSubdomain: String): List<KeycloakUserInfo> {
        val emailAddressSearchString = "%40$emailSubdomain."
        return searchUsers(emailAddressSearchString)
    }

    /**
     * Finds a Dataland user based on their email address. The specified email address must be
     * a precise match.
     * @param emailAddress the email address under which to find the user
     * @return the corresponding keycloak user info, or null if no Dataland user with that email address exists
     */
    fun findUserByEmail(emailAddress: String): KeycloakUserInfo? {
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/users?email=$emailAddress&exact=true"
        val response = getKeycloakResponse(url)
        return extractUsers(response).firstOrNull()
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
            val roles =
                mappingsRepresentation.realmMappings.map {
                    it.roleName
                }
            logger.info("Realm roles received from keycloak $roles for userId $userId")
            return roles
        } catch (e: JacksonException) {
            logger.warn("Failed to parse response from Keycloak. Response $response, exception: $e")
            return emptyList()
        }
    }

    /**
     * Get effective keycloak roles for a user by their userId. This will recurse all composite roles to get the result.
     */
    fun getCompositeUserRoleNames(userId: String): List<String> {
        val url = "$keycloakBaseUrl/admin/realms/datalandsecurity/users/$userId/role-mappings/realm/composite"
        val response = getKeycloakResponse(url)

        try {
            return objectMapper.readValue(response, object : TypeReference<List<KeycloakRoleRepresentation>>() {}).map { it.roleName }
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
