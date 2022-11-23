package org.dataland.datalandapikeymanager.utils

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.dataland.datalandapikeymanager.model.ApiKeyAndMetaInfo
import org.dataland.datalandapikeymanager.model.ApiKeyMetaInfo
import org.dataland.datalandapikeymanager.model.RevokeApiKeyResponse
import org.dataland.datalandapikeymanager.model.StoredHashedAndBase64EncodedApiKey
import org.keycloak.KeycloakPrincipal
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Base64
import java.util.HexFormat
import java.util.zip.CRC32

class ApiKeyManager {

    private val apiKeyParser = ApiKeyParser()

    // TODO temporary
    private val mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys = mutableMapOf<String, StoredHashedAndBase64EncodedApiKey>()
    // TODO temporary

    private val logger = LoggerFactory.getLogger(javaClass)

    private val keyByteLength = 40
    private val saltByteLength = 16
    private val hashByteLength = 32

    private val utf8Charset = Charsets.UTF_8

    private fun hashString(inputString: String, salt: ByteArray): ByteArray {
        val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(3)
            .withMemoryPowOfTwo(16)
            .withParallelism(1)
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

    private fun encodeToBase64(input: ByteArray): String {
        return Base64.getEncoder().encodeToString(input)
    }

    private fun decodeFromBase64(input: String): ByteArray {
        return Base64.getDecoder().decode(input)
    }

    private fun calculateCrc32Value(inputByteArray: ByteArray): Long {
        val crc32Instance = CRC32()
        crc32Instance.update(inputByteArray)
        return crc32Instance.value
    }

    private fun generateSalt(): ByteArray {
        return generateRandomByteArray(saltByteLength)
    }

    private fun generateApiKeySecretAndEncodeToHex(): String {
        return HexFormat.of().formatHex(generateRandomByteArray(keyByteLength))
    }

    private fun getKeycloakAuthenticationToken(): KeycloakAuthenticationToken {
        return SecurityContextHolder.getContext().authentication as KeycloakAuthenticationToken
    }

    private fun getKeycloakUserId(keycloakAuthenticationToken: KeycloakAuthenticationToken): String {
        var userIdByToken = ""
        val principal = keycloakAuthenticationToken.principal
        if (principal is KeycloakPrincipal<*>) {
            userIdByToken = principal.keycloakSecurityContext.token.subject
        }
        return userIdByToken
    }

    fun generateNewApiKey(daysValid: Int?): ApiKeyAndMetaInfo {
        val keycloakAuthenticationToken = getKeycloakAuthenticationToken()
        val keycloakUserId = getKeycloakUserId(keycloakAuthenticationToken)
        val keycloakUserIdBase64Encoded = encodeToBase64(keycloakUserId.toByteArray(utf8Charset))
        val keycloakRoles = keycloakAuthenticationToken.authorities.map { it.authority!! }.toList()
        val expiryDate: LocalDate? = if (daysValid == null || daysValid <= 0) null else LocalDate.now().plusDays(daysValid.toLong())
        val apiKeyMetaInfo = ApiKeyMetaInfo(keycloakUserId, keycloakRoles, expiryDate)

        val newSalt = generateSalt()
        val newApiKeyWithoutCrc32Value = keycloakUserIdBase64Encoded + "_" + generateApiKeySecretAndEncodeToHex()
        val newCrc32Value = calculateCrc32Value(newApiKeyWithoutCrc32Value.toByteArray(utf8Charset))
        val newApiKey = newApiKeyWithoutCrc32Value + "_" + newCrc32Value
        val newHashedApiKey = hashString(newApiKey, newSalt)
        val newHashedApiKeyBase64Encoded = encodeToBase64(newHashedApiKey)

        val storedHashedAndBase64EncodedApiKey = StoredHashedAndBase64EncodedApiKey(newHashedApiKeyBase64Encoded, apiKeyMetaInfo, encodeToBase64(newSalt))

        // TODO Storage/Replacement(!) process => needs to be in postgres. map is just temporary
        mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys[keycloakUserId] = storedHashedAndBase64EncodedApiKey
        // TODO

        logger.info("Generated Api Key with hashed value $newHashedApiKeyBase64Encoded and meta info $apiKeyMetaInfo.")
        return ApiKeyAndMetaInfo(newApiKey, apiKeyMetaInfo)
    }

    fun validateApiKey(apiKey: String): ApiKeyMetaInfo {
        val parsedApiKey = apiKeyParser.parseApiKeyAndValidateFormats(apiKey)
        val expectedCrc32Value = calculateCrc32Value(parsedApiKey.parsedApiKeyWithoutCrc32Value.toByteArray(utf8Charset)).toString()
        if (parsedApiKey.parsedCrc32Value != expectedCrc32Value) {
            throw IllegalArgumentException(
                "The cyclic redundancy check for the provided Api-Key failed. " +
                    "There must be parts of the Api-Key that are missing or it might have a typo."
            )
        }

        val keycloakUserId = decodeFromBase64(parsedApiKey.parsedKeycloakUserIdBase64Encoded).toString(utf8Charset)

        // TODO Validation process => needs to be in postgres. map is just temporary
        val storedHashedApiKey = mapOfKeycloakUserIdsAndStoredHashedAndBase64EncodedApiKeys[keycloakUserId]!!
        // TODO

        val salt = decodeFromBase64(storedHashedApiKey.saltBase64Encoded)
        val hashedApiKeyBase64Encoded = encodeToBase64(hashString(apiKey, salt))
        if (hashedApiKeyBase64Encoded == storedHashedApiKey.hashedApiKeyBase64Encoded) {
            logger.info("Validated Api Key with salt $salt and calculated hash value $hashedApiKeyBase64Encoded.")
        }
        return storedHashedApiKey.apiKeyMetaInfo
    }

    fun revokeApiKey(): RevokeApiKeyResponse {
        val keycloakAuthenticationToken = getKeycloakAuthenticationToken()
        val keycloakUserId = getKeycloakUserId(keycloakAuthenticationToken)
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
