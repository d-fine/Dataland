package org.dataland.datalandbackendutils.apikey

import org.dataland.datalandbackendutils.exceptions.ApiKeyFormatException
import org.dataland.datalandbackendutils.utils.EncodingUtils

/**
 * This class should be used to validate that a given api key has the reuired format and the correct checksum
 * before a request is sent to /api-keys/validateApiKey to prevent unneccessary traffic
 */
class ApiKeyPrevalidator {

    companion object {
        const val minPossibleCrc32Value = 0
        const val maxPossibleCrc32Value = 4294967295
        private const val numberOfUnderscoreDelimitersExpectedInApiKey = 2
    }

    private val regexForBase64 = Regex("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?\$")
    private val regexFor80HexCharacters = Regex("^[a-fA-F0-9]{80}\$") // length fixed
    private val regexForValidNumberBetweenZeroAndInfinite = Regex("^0\$|^[1-9]\\d*\$")

    private val charset = Charsets.UTF_8

    val validateApiKeyDelimitersExceptionMessage =
        "The received Api key does not contain exactly two underscore characters as it is expected."
    val validateKeycloakUserIdExceptionMessage =
        "The encoded user Id derived from the received Api key does not fulfill the Base64 format."
    val validateApiKeySecretExceptionMessage =
        "The Api key secret derived from the received Api key either " +
            "contains non-Hex characters, or it does not have exactly 80 characters."
    val validateCrc32ValueNoValidNumberExceptionMessage =
        "The CRC-32 value derived from the received Api key is not a valid number."
    val validateCrc32ValueOutOfRangeExceptionMessage =
        "The CRC-32 value derived from the received Api key is out of the expected range."
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

    private fun validateKeycloakUserId(potentialKeycloakUserIdBase64Encoded: String) {
        if (!regexForBase64.matches(potentialKeycloakUserIdBase64Encoded)) {
            throw ApiKeyFormatException(
                validateKeycloakUserIdExceptionMessage
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

    private fun validateCrc32Value(potentialCrc32Value: String) {
        if (!potentialCrc32Value.matches(regexForValidNumberBetweenZeroAndInfinite)) {
            throw ApiKeyFormatException(validateCrc32ValueNoValidNumberExceptionMessage)
        }
        if (potentialCrc32Value.toLong() !in minPossibleCrc32Value..maxPossibleCrc32Value) {
            throw ApiKeyFormatException(
                validateCrc32ValueOutOfRangeExceptionMessage
            )
        }
    }

    fun parseApiKey(receivedApiKey: String): ParsedApiKey {
        validateApiKeyDelimiters(receivedApiKey)

        val receivedApiKeySections = receivedApiKey.split("_")

        val parsedKeycloakUserIdBase64Encoded = receivedApiKeySections[0]
        val parsedApiKeySecret = receivedApiKeySections[1]
        val parsedApiKeyWithoutCrc32Value = parsedKeycloakUserIdBase64Encoded+"_"+parsedApiKeySecret
        val parsedCrc32Value = receivedApiKeySections[2]

        return ParsedApiKey(
            parsedKeycloakUserIdBase64Encoded,
            parsedApiKeySecret,
            parsedCrc32Value,
            parsedApiKeyWithoutCrc32Value
        )
    }

    private fun validateApiKeyFormat(parsedApiKey: ParsedApiKey) {
        validateKeycloakUserId(parsedApiKey.parsedKeycloakUserIdBase64Encoded)
        validateApiKeySecret(parsedApiKey.parsedApiKeySecret)
        validateCrc32Value(parsedApiKey.parsedCrc32Value)
    }

    private fun validateApiKeyChecksum(parsedApiKey: ParsedApiKey) {
        val expectedCrc32Value = EncodingUtils.calculateCrc32Value(
            parsedApiKey.parsedApiKeyWithoutCrc32Value.toByteArray(charset)
        ).toString()

        if (parsedApiKey.parsedCrc32Value != expectedCrc32Value) {
            throw IllegalArgumentException(validateApiKeyChecksumWrongValueExceptionMessage)
        }
    }

    /**
     * A method to validate the format of an api key and its checksum
     * @param receivedApiKey is the api key that the backend received and that needs to be prevalidated
     * @return the parsed api key
     */
    fun prevalidateApiKey(receivedApiKey: String): ParsedApiKey {
        val parsedApiKey = parseApiKey(receivedApiKey)
        validateApiKeyFormat(parsedApiKey)
        validateApiKeyChecksum(parsedApiKey)
        return parsedApiKey
    }
}
