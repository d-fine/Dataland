package org.dataland.datalandapikeymanager.services

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.RevokeApiKeyResponse
import org.dataland.datalandapikeymanager.model.StoredHashedAndBase64EncodedApiKey
import org.dataland.datalandbackendutils.apikey.ApiKeyPrevalidator
import org.dataland.datalandbackendutils.utils.EncodingUtils
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.security.SecureRandom
import java.time.LocalDate
import java.util.HexFormat
/**
 * A class for handling the generation, validation and revocation of an api key
 */
class ApiKeyManager {

    private companion object {
        private const val keyByteLength = 40
        private const val saltByteLength = 16
        private const val hashByteLength = 32

        private const val argon2Iterations = 3
        private const val argon2MemoryPowOfTwo = 16
        private const val argon2Parallelisms = 1
    }

    private val apiKeyPrevalidator = ApiKeyPrevalidator()

    private val validationMessageNoApiKeyRegistered = "Your Dataland account has no API key registered. " +
        "Please generate one."
    private val validationMessageWrongApiKey = "The API key you provided for your Dataland account is not correct."
    private val validationMessageExpiredApiKey = "The API key you provided for your Dataland account is expired."
    private val validationMessageSuccess = "The API key you provided was successfully validated."

    // TODO temporary use of map, but in the end we need a DB to store stuff
    private val mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys =
        mutableMapOf<String, StoredHashedAndBase64EncodedApiKey>()

    private val logger = LoggerFactory.getLogger(javaClass)

    private val utf8Charset = Charsets.UTF_8

    private fun hashString(inputString: String, salt: ByteArray): ByteArray {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(argon2Iterations)
            .withMemoryPowOfTwo(argon2MemoryPowOfTwo)
            .withParallelism(argon2Parallelisms)
            .withSalt(salt)
        val generator = Argon2BytesGenerator()
        generator.init(builder.build())
        val hash = ByteArray(hashByteLength)
        generator.generateBytes(inputString.toByteArray(), hash, 0, hash.size)
        return hash
    }

    private fun generateRandomByteArray(length: Int): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    private fun generateSalt(): ByteArray {
        return generateRandomByteArray(saltByteLength)
    }

    private fun generateApiKeySecretAndEncodeToHex(): String {
        return HexFormat.of().formatHex(generateRandomByteArray(keyByteLength))
    }

    private fun getAuthentication(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }

    private fun calculateExpiryDate(daysValid: Int?): LocalDate? {
        return if (daysValid == null || daysValid <= 0)
            null else
            LocalDate.now().plusDays(daysValid.toLong())
    }

    private fun generateApiKeyMetaInfo(daysValid: Int?): ApiKeyMetaInfo {
        val authentication = getAuthentication()
        // TODO: Fix usage of !! operator
        val keycloakUserId = authentication!!.name!!
        val keycloakRoles = authentication.authorities.map { it.authority!! }.toList()
        return ApiKeyMetaInfo(keycloakUserId, keycloakRoles, calculateExpiryDate(daysValid))
    }

    /**
     * A method that generates an api key which is valid for the specified number of days
     * @param daysValid the number of days the api key should be valid from time of generation
     * @return the api key and its meta info
     */
    fun generateNewApiKey(daysValid: Int?): ApiKeyAndMetaInfo {
        val apiKeyMetaInfo = generateApiKeyMetaInfo(daysValid)
        val keycloakUserIdBase64Encoded =
            EncodingUtils.encodeToBase64(apiKeyMetaInfo.keycloakUserId!!.toByteArray(utf8Charset))

        val newSalt = generateSalt()
        val newApiKeyWithoutCrc32Value = keycloakUserIdBase64Encoded + "_" + generateApiKeySecretAndEncodeToHex()
        val newCrc32Value = EncodingUtils.calculateCrc32Value(newApiKeyWithoutCrc32Value.toByteArray(utf8Charset))
        val newApiKey = newApiKeyWithoutCrc32Value + "_" + newCrc32Value
        val newHashedApiKeyBase64Encoded = EncodingUtils.encodeToBase64(hashString(newApiKey, newSalt))
        val storedHashedAndBase64EncodedApiKey = StoredHashedAndBase64EncodedApiKey(
            newHashedApiKeyBase64Encoded,
            apiKeyMetaInfo,
            EncodingUtils.encodeToBase64(newSalt)
        )
        // TODO Storage/Replacement(!) process => needs to be in postgres. map is just temporary
        mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys[apiKeyMetaInfo.keycloakUserId] =
            storedHashedAndBase64EncodedApiKey
        logger.info("Generated Api Key with hashed value $newHashedApiKeyBase64Encoded and meta info $apiKeyMetaInfo.")
        return ApiKeyAndMetaInfo(newApiKey, apiKeyMetaInfo)
    }

    private fun checkIfApiKeyForUserIsCorrectAndReturnApiKeyMetaInfoWithActivityStatus(
        receivedApiKeyHashedAndBase64Encoded: String,
        storedHashedApiKeyOfUser: StoredHashedAndBase64EncodedApiKey,
        keycloakUserId: String
    ): ApiKeyMetaInfo {
        return if (receivedApiKeyHashedAndBase64Encoded != storedHashedApiKeyOfUser.hashedApiKeyBase64Encoded) {
            logger.info("The provided Api Key for the user $keycloakUserId is not correct.")
            ApiKeyMetaInfo(active = false, validationMessage = validationMessageWrongApiKey)
        } else {
            val activityStatus = storedHashedApiKeyOfUser.apiKeyMetaInfo.expiryDate!!.isAfter(LocalDate.now())
            logger.info(
                "Validated Api Key with salt ${storedHashedApiKeyOfUser.saltBase64Encoded} and calculated hash " +
                    "value $receivedApiKeyHashedAndBase64Encoded. " +
                    "The activity status of the API key is $activityStatus."
            )
            var validationMessageToReturn = validationMessageSuccess
            if (!activityStatus) {
                validationMessageToReturn = validationMessageExpiredApiKey
            }
            storedHashedApiKeyOfUser.apiKeyMetaInfo.copy(active = activityStatus, validationMessage = validationMessageToReturn)
        }
    }

    /**
     * Validates a specified api key
     * @param receivedApiKey the received api key to be validated
     * @return the found api keys meta info
     */
    fun validateApiKey(receivedApiKey: String): ApiKeyMetaInfo {
        val receivedAndParsedApiKey = apiKeyPrevalidator.parseApiKey(receivedApiKey)
        val keycloakUserId = EncodingUtils.decodeFromBase64(receivedAndParsedApiKey.parsedKeycloakUserIdBase64Encoded)
            .toString(utf8Charset)
        // TODO Retrieval process => needs to be in postgres. map is just temporary
        val storedHashedApiKeyOfUser = mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys[keycloakUserId]
        if (storedHashedApiKeyOfUser == null) {
            logger.info("Dataland user with the Keycloak user Id $keycloakUserId has no API key registered.")
            return ApiKeyMetaInfo(active = false, validationMessage = validationMessageNoApiKeyRegistered)
        }
        val salt = EncodingUtils.decodeFromBase64(storedHashedApiKeyOfUser.saltBase64Encoded)
        val receivedApiKeyHashedAndBase64Encoded = EncodingUtils.encodeToBase64(hashString(receivedApiKey, salt))

        return checkIfApiKeyForUserIsCorrectAndReturnApiKeyMetaInfoWithActivityStatus(
            receivedApiKeyHashedAndBase64Encoded, storedHashedApiKeyOfUser, keycloakUserId
        )
    }

    /**
     * Revokes the api key of the authenticating user
     * @return the result of the attempted revocation as a status flag and a message
     */
    fun revokeApiKey(): RevokeApiKeyResponse {
        // TODO: Fix the !! operator
        val keycloakUserId = getAuthentication()!!.name
        val revokementProcessSuccessful: Boolean
        val revokementProcessMessage: String
        // TODO Checking if Api key exists => needs to be in postgres. map is just temporary
        if (!mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys.containsKey(keycloakUserId)) {
            revokementProcessSuccessful = false
            revokementProcessMessage = "No revokement took place since there is no Api key registered for the " +
                "Keycloak user Id $keycloakUserId."
        }
        // TODO Deleting process => needs to be in postgres. map is just temporary
        else {
            mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys.remove(keycloakUserId)
            revokementProcessSuccessful = true
            revokementProcessMessage = "The Api key for the Keycloak user Id $keycloakUserId was successfully " +
                "removed from storage."
        }
        return RevokeApiKeyResponse(revokementProcessSuccessful, revokementProcessMessage)
    }
}
