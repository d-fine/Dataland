package org.dataland.datalandbackendutils.apikey

import org.dataland.datalandbackendutils.exceptions.ApiKeyFormatException
import org.dataland.datalandbackendutils.utils.EncodingUtils

/**
 * This class should be used to validate that a given api key has the reuired format and the correct checksum
 * before a request is sent to /api-keys/validateApiKey to prevent unneccessary traffic
 */
class ApiKeyPrevalidator { // TODO not too happy with that name

    // private val logger = LoggerFactory.getLogger(javaClass) // TODO log some stuff while validating maybe?

    private val regexForBase64 = Regex("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?\$")
    private val regexFor80HexCharacters = Regex("^[a-fA-F0-9]{80}\$") // length fixed
    private val regexForValidNumberBetweenZeroAndInfinite = Regex("^0\$|^[1-9]\\d*\$")

    private val charset = Charsets.UTF_8

    private fun validateApiKeyDelimiters(receivedApiKey: String) {
        if (receivedApiKey.count { it.toString() == "_" } != 2) {
            throw ApiKeyFormatException(
                "The received Api key does not contain exactly two underscore characters " +
                    "as it is expected."
            )
        }
    }

    private fun validateKeycloakUserId(potentialKeycloakUserIdBase64Encoded: String) {
        if (!regexForBase64.matches(potentialKeycloakUserIdBase64Encoded)) {
            throw ApiKeyFormatException(
                "The encoded user Id derived from the received Api key does not fulfill " +
                    "the Base64 format."
            )
        }
    }

    private fun validateApiKeySecret(potentialApiKeySecret: String) {
        if (!regexFor80HexCharacters.matches(potentialApiKeySecret)) {
            throw ApiKeyFormatException(
                "The Api key secret derived from the received Api key either " +
                    "contains non-Hex characters, or it does not have exactly 80 characters."
            )
        }
    }

    private fun validateCrc32Value(potentialCrc32Value: String) {
        if (!potentialCrc32Value.matches(regexForValidNumberBetweenZeroAndInfinite)) {
            throw ApiKeyFormatException("The CRC-32 value derived from the received Api key is not a valid number.")
        }
        if (potentialCrc32Value.toLong() !in 0..Long.MAX_VALUE) {
            throw ApiKeyFormatException(
                "The CRC-32 value derived from the received Api key is out of the " +
                    "expected range."
            )
        }
    }

    private fun parseApiKey(receivedApiKey: String): ParsedApiKey {
        validateApiKeyDelimiters(receivedApiKey)

        val parsedKeycloakUserIdBase64Encoded = receivedApiKey.substringBefore("_")
        val parsedCrc32Value = receivedApiKey.substringAfterLast("_")
        val parsedApiKeySecret = receivedApiKey.substringAfter("_").substringBefore("_")
        val parsedApiKeyWithoutCrc32Value = receivedApiKey.substringBeforeLast("_")

        return ParsedApiKey(parsedKeycloakUserIdBase64Encoded,
            parsedApiKeySecret,
            parsedCrc32Value,
            parsedApiKeyWithoutCrc32Value)
    }

    private fun validateApiKeyFormat(parsedApiKey: ParsedApiKey) {
        validateKeycloakUserId(parsedApiKey.parsedKeycloakUserIdBase64Encoded)
        validateApiKeySecret(parsedApiKey.parsedApiKeySecret)
        validateCrc32Value(parsedApiKey.parsedCrc32Value)
    }

    private fun validateApiKeyChecksum(parsedApiKey: ParsedApiKey) {
        val expectedCrc32Value = EncodingUtils.calculateCrc32Value(
            parsedApiKey.parsedApiKeyWithoutCrc32Value.toByteArray(charset))
            .toString()

        if (parsedApiKey.parsedCrc32Value != expectedCrc32Value) {
            throw ApiKeyFormatException(
                "The cyclic redundancy check for the provided Api-Key failed. " +
                    "There must be parts of the Api-Key that are missing or it might have a typo."
            )
        }
    }

    /**
     * A method to validate the format of an api key and its checksum
     * @param apiKey is the api key to prevalidate
     * @return the parsed api key
     */
    fun prevalidateApiKey(apiKey: String): ParsedApiKey {
        val parsedApiKey = parseApiKey(apiKey)
        validateApiKeyFormat(parsedApiKey)
        validateApiKeyChecksum(parsedApiKey)
        return parsedApiKey
    }
}
