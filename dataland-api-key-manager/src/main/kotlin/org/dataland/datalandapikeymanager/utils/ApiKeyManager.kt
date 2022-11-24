package org.dataland.datalandapikeymanager.utils

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

    private val apiKeyPreValidator = ApiKeyPrevalidator()

    // TODO temporary
    private val mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys =
        mutableMapOf<String, StoredHashedAndBase64EncodedApiKey>()
    // TODO temporary

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

    private fun getKeycloakUserId(authentication: Authentication): String {
        val userIdByToken = ""
/*        val principal = authentication.principal
        if (principal is KeycloakPrincipal<*>) {
            userIdByToken = principal.keycloakSecurityContext.token.subject
        }*/
        return userIdByToken
    }

    /**
     * A method that generates an api key which is valid for the specified number of days
     * @param daysValid the number of days the api key should be valid from time of generation
     * @return the api key and its meta info
     */
    fun generateNewApiKey(daysValid: Int?): ApiKeyAndMetaInfo {
        val keycloakAuthenticationToken = getAuthentication()
        // TODO: Fix usage of !! operator
        val keycloakUserId = getKeycloakUserId(keycloakAuthenticationToken!!)
        val keycloakUserIdBase64Encoded = EncodingUtils.encodeToBase64(keycloakUserId.toByteArray(utf8Charset))
        val keycloakRoles = keycloakAuthenticationToken.authorities.map { it.authority!! }.toList()
        val expiryDate: LocalDate? = if (daysValid == null || daysValid <= 0)
            null else
            LocalDate.now().plusDays(daysValid.toLong())
        val apiKeyMetaInfo = ApiKeyMetaInfo(keycloakUserId, keycloakRoles, expiryDate)

        val newSalt = generateSalt()
        val newApiKeyWithoutCrc32Value = keycloakUserIdBase64Encoded + "_" + generateApiKeySecretAndEncodeToHex()
        val newCrc32Value = EncodingUtils.calculateCrc32Value(newApiKeyWithoutCrc32Value.toByteArray(utf8Charset))
        val newApiKey = newApiKeyWithoutCrc32Value + "_" + newCrc32Value
        val newHashedApiKey = hashString(newApiKey, newSalt)
        val newHashedApiKeyBase64Encoded = EncodingUtils.encodeToBase64(newHashedApiKey)

        val storedHashedAndBase64EncodedApiKey = StoredHashedAndBase64EncodedApiKey(
            newHashedApiKeyBase64Encoded,
            apiKeyMetaInfo,
            EncodingUtils.encodeToBase64(newSalt)
        )

        // TODO Storage/Replacement(!) process => needs to be in postgres. map is just temporary
        mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys[keycloakUserId] = storedHashedAndBase64EncodedApiKey
        // TODO

        logger.info("Generated Api Key with hashed value $newHashedApiKeyBase64Encoded and meta info $apiKeyMetaInfo.")
        return ApiKeyAndMetaInfo(newApiKey, apiKeyMetaInfo)
    }

    /**
     * Validates a specified api key
     * @param apiKey the api key to be validated
     * @return the found api keys meta info
     */
    fun validateApiKey(apiKey: String): ApiKeyMetaInfo {
        val parsedApiKey = apiKeyPreValidator.prevalidateApiKey(apiKey)

        val keycloakUserId = EncodingUtils.decodeFromBase64(parsedApiKey.parsedKeycloakUserIdBase64Encoded)
            .toString(utf8Charset)

        // TODO Validation process => needs to be in postgres. map is just temporary
        val storedHashedApiKey = mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys[keycloakUserId]!!
        // TODO

        // TODO what if the keycloak user id has no entry in the map?

        val salt = EncodingUtils.decodeFromBase64(storedHashedApiKey.saltBase64Encoded)
        val hashedApiKeyBase64Encoded = EncodingUtils.encodeToBase64(hashString(apiKey, salt))
        if (hashedApiKeyBase64Encoded == storedHashedApiKey.hashedApiKeyBase64Encoded) {
            logger.info("Validated Api Key with salt $salt and calculated hash value $hashedApiKeyBase64Encoded.")
        }
        return storedHashedApiKey.apiKeyMetaInfo
    }

    /**
     * Revokes the api key of the authenticating user
     * @return the result of the attempted revocation as a status flag and a message
     */
    fun revokeApiKey(): RevokeApiKeyResponse {
        val authetication = getAuthentication()
        // Todo: Fix the !! operator
        val keycloakUserId = getKeycloakUserId(authetication!!)
        val revokementProcessSuccessful: Boolean
        val revokementProcessMessage: String

        if (

            // TODO Checking if Api key exists => needs to be in postgres. map is just temporary
            !mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys.containsKey(keycloakUserId)
            // TODO

        ) {
            revokementProcessSuccessful = false
            revokementProcessMessage = "No revokement took place since there is no Api key registered for the " +
                "Keycloak user Id $keycloakUserId."
        } else {

            // TODO Deleting process => needs to be in postgres. map is just temporary
            mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys.remove(keycloakUserId)
            // TODO
            revokementProcessSuccessful = true
            revokementProcessMessage = "The Api key for the Keycloak user Id $keycloakUserId was successfully " +
                "removed from storage."
        }

        return RevokeApiKeyResponse(revokementProcessSuccessful, revokementProcessMessage)
    }
}
