package org.dataland.datalandbackendutils.apikey

import org.dataland.datalandbackendutils.exceptions.ApiKeyFormatException
import org.dataland.datalandbackendutils.utils.EncodingUtils.calculateCrc32Value
import org.dataland.datalandbackendutils.utils.EncodingUtils.decodeFromBase64
import org.dataland.datalandbackendutils.utils.EncodingUtils.encodeToBase64
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import java.security.SecureRandom
import java.util.HexFormat

/**
 * This class should be used to validate that a given api key has the required format and the correct checksum
 * before a request is sent to /api-keys/validateApiKey to prevent unnecessary traffic
 */
class ApiKeyUtility {

    companion object {
        private const val keyByteLength = 40
    }

    private val numberOfUnderscoreDelimitersExpectedInApiKey = 2

    private val regexFor80HexCharacters = Regex("^[a-fA-F0-9]{${keyByteLength * 2}}\$")

    private val charset = Charsets.UTF_8

    val validateApiKeyDelimitersExceptionMessage =
        "The received Api key does not contain exactly two underscore characters as it is expected."
    val validateKeycloakUserIdExceptionMessage =
        "The encoded user Id derived from the received Api key does not fulfill the Base64 format."
    val validateApiKeySecretExceptionMessage =
        "The Api key secret derived from the received Api key either " +
            "contains non-Hex characters, or it does not have exactly 80 characters."
    val validateApiKeyChecksumWrongValueExceptionMessage =
        "The cyclic redundancy check for the provided Api-Key failed. " +
            "There must be parts of the Api-Key that are missing or it might have a typo."

    private fun validateApiKeyDelimiters(receivedApiKey: String) {
        if (receivedApiKey.count { it.toString() == "_" } != numberOfUnderscoreDelimitersExpectedInApiKey) {
            throw ApiKeyFormatException(
                validateApiKeyDelimitersExceptionMessage
            )
        }
    }

    private fun validateApiKeySecret(potentialApiKeySecret: String) {
        if (!regexFor80HexCharacters.matches(potentialApiKeySecret)) {
            throw ApiKeyFormatException(
                validateApiKeySecretExceptionMessage
            )
        }
    }

    /**
     * Parses an api key and splits it into its component
     * @param receivedApiKey the api key to be parsed
     * @return the parsed api key
     */
    fun parseApiKey(receivedApiKey: String): ParsedApiKey {
        validateApiKeyDelimiters(receivedApiKey)

        val receivedApiKeySections = receivedApiKey.split("_")
        val parsedKeycloakUserIdBase64Encoded = receivedApiKeySections[0]
        val parsedApiKeySecret = receivedApiKeySections[1]
        val parsedCrc32Value = receivedApiKeySections[2]

        val expectedCrc32Value = getCrc(parsedKeycloakUserIdBase64Encoded, parsedApiKeySecret)

        if (parsedCrc32Value != expectedCrc32Value) {
            throw ApiKeyFormatException(validateApiKeyChecksumWrongValueExceptionMessage)
        }

        val keycloakUserId = try {
            String(decodeFromBase64(parsedKeycloakUserIdBase64Encoded), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            throw ApiKeyFormatException(validateKeycloakUserIdExceptionMessage, e)
        }
        validateApiKeySecret(parsedApiKeySecret)
        return ParsedApiKey(keycloakUserId, parsedApiKeySecret)
    }

    private fun getCrc(keycloakUserIdBase64Encoded: String, apiKeySecret: String): String {
        val apiKeyWithoutCrc32Value = keycloakUserIdBase64Encoded + "_" + apiKeySecret

        return calculateCrc32Value(
            apiKeyWithoutCrc32Value.toByteArray(charset)
        ).toString()
    }

    /**
     * generates a random secret (in hex format)
     */
    fun generateApiKeySecret(): String {
        val bytes = ByteArray(keyByteLength)
        SecureRandom().nextBytes(bytes)
        return HexFormat.of().formatHex(bytes)
    }

    /**
     * verifies whether an encoded secret and a secret match
     */
    fun matchesSecretAndEncodedSecret(secret: String, encodedSecret: String): Boolean {
        return Argon2PasswordEncoder().matches(secret, encodedSecret)
    }

    /**
     * Encodes a secret to a storable format
     */
    fun encodeSecret(secret: String): String {
        return Argon2PasswordEncoder().encode(secret)
    }

    /**
     * converts a ParsedApiKey Object (with secret and userId) to an API-Key in the correct parseable format.
     */
    fun convertToApiKey(parsedApiKey: ParsedApiKey): String {
        val keycloakUserIdBase64Encoded = encodeToBase64(parsedApiKey.keycloakUserId.toByteArray())
        return keycloakUserIdBase64Encoded + "_" +
            parsedApiKey.apiKeySecret + "_" +
            getCrc(keycloakUserIdBase64Encoded = keycloakUserIdBase64Encoded, apiKeySecret = parsedApiKey.apiKeySecret)
    }
}
