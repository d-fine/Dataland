package org.dataland.datalandapikeymanager.services

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.dataland.datalandapikeymanager.entities.StoredHashedAndBase64EncodedApiKeyEntity
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.RevokeApiKeyResponse
import org.dataland.datalandapikeymanager.model.StoredHashedAndBase64EncodedApiKey
import org.dataland.datalandapikeymanager.repositories.StoredHashedAndBase64EncodedApiKeyRepository
import org.dataland.datalandbackendutils.apikey.ApiKeyPrevalidator
import org.dataland.datalandbackendutils.utils.EncodingUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.HexFormat
/**
 * A class for handling the generation, validation and revocation of an api key
 */
@Component("ApiKeyManager")
class ApiKeyManager
(@Autowired private val storedHashedAndBase64EncodedApiKeyRepository: StoredHashedAndBase64EncodedApiKeyRepository)
{

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

    private val revokementMessageNonExistingApiKey = "Your Dataland account has no API key registered. Therefore no " +
            "revokement took place."
    private val revokementMessageSuccess = "The API key for your Dataland account was successfully revoked."

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

    private fun getAuthentication(): Authentication {
        return SecurityContextHolder.getContext().authentication
    }

    private fun getKeycloakUserIdBase64Encoded(authentication: Authentication): String{
        return EncodingUtils.encodeToBase64(authentication.name.toByteArray(utf8Charset))
    }

    private fun calculateExpiryDate(daysValid: Int?): LocalDateTime? {
        return if (daysValid == null)
            null else
            LocalDateTime.now(ZoneOffset.UTC).plusDays(daysValid.toLong())
    }

    private fun generateApiKeyMetaInfo(daysValid: Int?): ApiKeyMetaInfo {
        val authentication = getAuthentication()
        val keycloakUserIdBase64Encoded = getKeycloakUserIdBase64Encoded(authentication)
        val keycloakRoles = authentication.authorities.map { it.authority!! }.toList()
        return ApiKeyMetaInfo(keycloakUserIdBase64Encoded, keycloakRoles, calculateExpiryDate(daysValid))
    }

    /**
     * A method that generates an API key which is valid for the specified number of days
     * @param daysValid the number of days the API key should be valid from time of generation
     * @return the API key and its meta info
     */
    fun generateNewApiKey(daysValid: Int?): ApiKeyAndMetaInfo {
        val apiKeyMetaInfo = generateApiKeyMetaInfo(daysValid)

        val newSalt = generateSalt()
        val newApiKeyWithoutCrc32Value = apiKeyMetaInfo.keycloakUserIdBase64Encoded + "_" + generateApiKeySecretAndEncodeToHex()
        val newCrc32Value = EncodingUtils.calculateCrc32Value(newApiKeyWithoutCrc32Value.toByteArray(utf8Charset))
        val newApiKey = newApiKeyWithoutCrc32Value + "_" + newCrc32Value
        val newApiKeyHashedAndBase64Encoded = EncodingUtils.encodeToBase64(hashString(newApiKey, newSalt))

        val hashedAndBase64EncodedApiKeyEntityToStore = StoredHashedAndBase64EncodedApiKeyEntity(
            newApiKeyHashedAndBase64Encoded,
            apiKeyMetaInfo.keycloakUserIdBase64Encoded!!,
            apiKeyMetaInfo.keycloakRoles!!,
            apiKeyMetaInfo.expiryDate,
            EncodingUtils.encodeToBase64(newSalt)
        )

        storedHashedAndBase64EncodedApiKeyRepository.save(hashedAndBase64EncodedApiKeyEntityToStore)
        logger.info("Generated Api Key with hashed value $newApiKeyHashedAndBase64Encoded and meta info $apiKeyMetaInfo.")
        return ApiKeyAndMetaInfo(newApiKey, apiKeyMetaInfo)
    }

    private fun checkIfApiKeyForUserIsCorrectAndReturnApiKeyMetaInfoWithActivityStatus(
        receivedApiKeyHashedAndBase64Encoded: String,
        storedHashedAndBase64EncodedApiKeyOfUser: StoredHashedAndBase64EncodedApiKey,
        keycloakUserIdBase64Encoded: String
    ): ApiKeyMetaInfo {
        return if (receivedApiKeyHashedAndBase64Encoded != storedHashedAndBase64EncodedApiKeyOfUser.apiKeyHashedAndBase64Encoded) {
            logger.info("The provided Api Key for the encoded Keycloak user Id $keycloakUserIdBase64Encoded is not correct.")
            ApiKeyMetaInfo(active = false, validationMessage = validationMessageWrongApiKey)
        } else {
            val activityStatus =
                storedHashedAndBase64EncodedApiKeyOfUser.apiKeyMetaInfo.expiryDate?.isAfter(LocalDateTime.now(ZoneOffset.UTC))
                    ?: true
            logger.info(
                "Validated Api Key with encoded salt ${storedHashedAndBase64EncodedApiKeyOfUser.saltBase64Encoded} and " +
                        "calculated hashed and encoded value $receivedApiKeyHashedAndBase64Encoded. " +
                    "The activity status of the API key is $activityStatus."
            )
            var validationMessageToReturn = validationMessageSuccess
            if (!activityStatus) {
                validationMessageToReturn = validationMessageExpiredApiKey
            }
            storedHashedAndBase64EncodedApiKeyOfUser.apiKeyMetaInfo.copy(
                active = activityStatus,
                validationMessage = validationMessageToReturn
            )
        }
    }

    /**
     * Validates a specified api key
     * @param receivedApiKey the received API key to be validated
     * @return the found api keys meta info
     */
    fun validateApiKey(receivedApiKey: String): ApiKeyMetaInfo {
        val receivedAndParsedApiKey = apiKeyPrevalidator.parseApiKey(receivedApiKey)

        val storedHashedAndBase64EncodedApiKeyOfUserOptional = storedHashedAndBase64EncodedApiKeyRepository.findById(
            receivedAndParsedApiKey.parsedKeycloakUserIdBase64Encoded
        )

        if(storedHashedAndBase64EncodedApiKeyOfUserOptional.isEmpty){
            logger.info("Dataland user with the encoded Keycloak user Id " +
                    "${receivedAndParsedApiKey.parsedKeycloakUserIdBase64Encoded} has no API key registered.")
            return ApiKeyMetaInfo(active = false, validationMessage = validationMessageNoApiKeyRegistered)
        }

        val storedHashedAndBase64EncodedApiKeyOfUser = storedHashedAndBase64EncodedApiKeyOfUserOptional.get().toApiModel()
        val salt = EncodingUtils.decodeFromBase64(storedHashedAndBase64EncodedApiKeyOfUser.saltBase64Encoded)
        val receivedApiKeyHashedAndBase64Encoded = EncodingUtils.encodeToBase64(hashString(receivedApiKey, salt))

        return checkIfApiKeyForUserIsCorrectAndReturnApiKeyMetaInfoWithActivityStatus(
            receivedApiKeyHashedAndBase64Encoded, storedHashedAndBase64EncodedApiKeyOfUser, receivedAndParsedApiKey.parsedKeycloakUserIdBase64Encoded
        )
    }

    /**
     * Revokes the API key of the authenticating user
     * @return the result of the attempted revocation as a status flag and a message
     */
    fun revokeApiKey(): RevokeApiKeyResponse {
        val authentication = getAuthentication()
        val keycloakUserIdBase64Encoded = getKeycloakUserIdBase64Encoded(authentication)
        val revokementProcessSuccessful: Boolean
        val revokementProcessMessage: String

        if (storedHashedAndBase64EncodedApiKeyRepository.findById(
                keycloakUserIdBase64Encoded
            ).isEmpty) {
            revokementProcessSuccessful = false
            revokementProcessMessage = revokementMessageNonExistingApiKey
            logger.info("No revokement took place since there is no API key registered for the encoded " +
                    "Keycloak user Id $keycloakUserIdBase64Encoded.")
        }

        else {
            storedHashedAndBase64EncodedApiKeyRepository.deleteById(keycloakUserIdBase64Encoded)
            revokementProcessSuccessful = true
            revokementProcessMessage = revokementMessageSuccess
            logger.info("The API key for the encoded Keycloak user Id $keycloakUserIdBase64Encoded was successfully " +
                "removed from storage.")
        }
        return RevokeApiKeyResponse(revokementProcessSuccessful, revokementProcessMessage)
    }
}
