package org.dataland.datalandapikeymanager.services

import org.dataland.datalandapikeymanager.entities.ApiKeyEntity
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.RevokeApiKeyResponse
import org.dataland.datalandapikeymanager.repositories.ApiKeyRepository
import org.dataland.datalandbackendutils.apikey.ApiKeyUtility
import org.dataland.datalandbackendutils.apikey.ParsedApiKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * A class for handling the generation, validation and revocation of an api key
 */
@Component("ApiKeyManager")
class ApiKeyManager
(@Autowired private val apiKeyRepository: ApiKeyRepository) {

    companion object {
        private const val secondsInADay = 86400
    }
    private val validationMessageNoApiKeyRegistered = "Your Dataland account has no API key registered. " +
        "Please generate one."
    private val validationMessageWrongApiKey = "The API key you provided for your Dataland account is not correct."
    private val validationMessageExpiredApiKey = "The API key you provided for your Dataland account is expired."
    private val validationMessageSuccess = "The API key you provided was successfully validated."

    private val revokementMessageNonExistingApiKey = "Your Dataland account has no API key registered. Therefore no " +
        "revokement took place."
    private val revokementMessageSuccess = "The API key for your Dataland account was successfully revoked."

    private val logger = LoggerFactory.getLogger(javaClass)

    private val apiKeyUtility = ApiKeyUtility()

    private fun getAuthentication(): Authentication {
        return SecurityContextHolder.getContext().authentication
    }

    private fun calculateExpiryDate(daysValid: Int?): Long? {
        return if (daysValid == null) {
            null
        } else {
            (daysValid * secondsInADay) + Instant.now().epochSecond
        }
    }

    private fun generateApiKeyMetaInfo(daysValid: Int?): ApiKeyMetaInfo {
        val authentication = getAuthentication()
        val keycloakUserId = authentication.name
        val keycloakRoles = authentication.authorities.map { it.authority!! }.toList()
        return ApiKeyMetaInfo(keycloakUserId, keycloakRoles, calculateExpiryDate(daysValid))
    }

    /**
     * A method that generates an API key which is valid for the specified number of days
     * @param daysValid the number of days the API key should be valid from time of generation
     * @return the API key and its meta info
     */
    fun generateNewApiKey(daysValid: Int?): ApiKeyAndMetaInfo {
        val apiKeyMetaInfo = generateApiKeyMetaInfo(daysValid)

        val secret = apiKeyUtility.generateApiKeySecret()
        val parsedApiKey = ParsedApiKey(apiKeyMetaInfo.keycloakUserId!!, secret)
        val encodedSecret = apiKeyUtility.encodeSecret(secret)
        val apiKeyEntity = ApiKeyEntity(encodedSecret, apiKeyMetaInfo)
        apiKeyRepository.save(apiKeyEntity)
        logger.info(
            "Generated Api Key with encoded secret value $encodedSecret and meta info $apiKeyMetaInfo."
        )
        return ApiKeyAndMetaInfo(apiKeyUtility.convertToApiKey(parsedApiKey), apiKeyMetaInfo)
    }

    /**
     * Validates a specified api key
     * @param receivedApiKey the received API key to be validated
     * @return the found api keys meta info
     */
    fun validateApiKey(receivedApiKey: String): ApiKeyMetaInfo {
        val receivedAndParsedApiKey = apiKeyUtility.parseApiKey(receivedApiKey)

        val apiKeyEntityOptional = apiKeyRepository.findById(
            receivedAndParsedApiKey.keycloakUserId
        )

        val result: ApiKeyMetaInfo
        if (apiKeyEntityOptional.isEmpty) {
            logger.info(
                "Dataland user with the encoded Keycloak user Id " +
                    "${receivedAndParsedApiKey.keycloakUserId} has no API key registered."
            )
            result = ApiKeyMetaInfo(active = false, validationMessage = validationMessageNoApiKeyRegistered)
        } else if (!apiKeyUtility.matchesSecretAndEncodedSecret(
                receivedAndParsedApiKey.apiKeySecret,
                apiKeyEntityOptional.get().encodedSecret
            )
        ) {
            result = ApiKeyMetaInfo(active = false, validationMessage = validationMessageWrongApiKey)
        } else {
            val apiKeyEntity = apiKeyEntityOptional.get()
            val expiryDateOfApiKey = apiKeyEntity.expiryDate
            val currentTime = Instant.now().epochSecond
            val active =
                (expiryDateOfApiKey ?: currentTime) >= currentTime
            logger.info(
                "Validated Api Key $receivedApiKey " +
                    "The activity status of the API key is $active."
            )

            var validationMessage = validationMessageSuccess
            if (!active) {
                validationMessage = validationMessageExpiredApiKey
            }
            result = ApiKeyMetaInfo(
                apiKeyEntity.keycloakUserId,
                apiKeyEntity.keycloakRoles,
                apiKeyEntity.expiryDate,
                active,
                validationMessage
            )
        }
        return result
    }

    /**
     * Revokes the API key of the authenticating user
     * @return the result of the attempted revocation as a status flag and a message
     */
    fun revokeApiKey(): RevokeApiKeyResponse {
        val authentication = getAuthentication()
        val revokementProcessSuccessful: Boolean
        val revokementProcessMessage: String

        val keycloakUserId = authentication.name!!
        val apiKeyEntityOptional = apiKeyRepository.findById(keycloakUserId)
        if (apiKeyEntityOptional.isEmpty
        ) {
            revokementProcessSuccessful = false
            revokementProcessMessage = revokementMessageNonExistingApiKey
            logger.info(
                "Revokement failed, no API key registered for the Keycloak user Id $keycloakUserId"
            )
        } else {
            apiKeyRepository.delete(apiKeyEntityOptional.get())
            revokementProcessSuccessful = true
            revokementProcessMessage = revokementMessageSuccess
            logger.info(
                "The API key for the  Keycloak user Id $keycloakUserId was successfully removed from storage."
            )
        }
        return RevokeApiKeyResponse(revokementProcessSuccessful, revokementProcessMessage)
    }
}
