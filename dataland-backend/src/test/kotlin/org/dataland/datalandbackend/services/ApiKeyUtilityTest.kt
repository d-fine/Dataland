package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.apikey.ApiKeyUtility
import org.dataland.datalandbackendutils.apikey.ParsedApiKey
import org.dataland.datalandbackendutils.exceptions.ApiKeyFormatException
import org.dataland.datalandbackendutils.utils.EncodingUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ApiKeyUtilityTest {

    private val apiKeyUtility = ApiKeyUtility()

    private val testKeycloakUserId = "c5ef10b1-de23-4a01-9005-e62ea226ee83"
    private val testApiKeyBase64EncodedKeycloakUserId = "YzVlZjEwYjEtZGUyMy00YTAxLTkwMDUtZTYyZWEyMjZlZTgz"
    private val testApiKeySecret = "da030a8c290f43a022bdb3da59f6ee4d7b5e290997fbdb3a22f1a7ce5a1a51c87f7666a3476a950b"
    private val testApiKeyCrc32Value = "465063745"

    private fun getCrc(keycloakUserIdBase64Encoded: String, apiKeySecret: String): String {
        val parsedApiKeyWithoutCrc32Value = keycloakUserIdBase64Encoded + "_" + apiKeySecret

        return EncodingUtils.calculateCrc32Value(
            parsedApiKeyWithoutCrc32Value.toByteArray()
        ).toString()
    }

    private fun parseBrokenApiKeyAndAssertThrownMessage(
        brokenApiKey: String,
        expectedMessage: String
    ) {
        val thrown = assertThrows<ApiKeyFormatException> {
            apiKeyUtility.parseApiKey(brokenApiKey)
        }
        assertEquals(
            expectedMessage,
            thrown.message
        )
    }

    @Test
    fun `check if correct api key can be parsed`() {
        val apiKey = testApiKeyBase64EncodedKeycloakUserId + "_" + testApiKeySecret + "_" + testApiKeyCrc32Value
        val parsedApiKey = apiKeyUtility.parseApiKey(apiKey)
        val expectedParsedApiKey = ParsedApiKey(
            testKeycloakUserId, testApiKeySecret
        )
        assertEquals(expectedParsedApiKey, parsedApiKey)
    }

    @Test
    fun `check if exception thrown if the provided api key does not have the exact number of delimiters`() {
        val apiKeyWithOneTooManyDelimiter =
            testApiKeyBase64EncodedKeycloakUserId + "_" +
                testApiKeySecret + "_" +
                testApiKeyCrc32Value + "_"
        val totallyRandomString = "aksjflakjsglkajsglkjas"
        val expectedApiKeyFormatExceptionMessage = apiKeyUtility.validateApiKeyDelimitersExceptionMessage
        listOf(apiKeyWithOneTooManyDelimiter, totallyRandomString).forEach { brokenApiKey ->
            parseBrokenApiKeyAndAssertThrownMessage(brokenApiKey, expectedApiKeyFormatExceptionMessage)
        }
    }

    @Test
    fun `check if exception thrown if the included keycloak user Id is not in Base64 format`() {
        val badUserId = ")$testApiKeyBase64EncodedKeycloakUserId"
        val apiKeyWithInvalidBase64CharacterInUserId =
            badUserId + "_" + testApiKeySecret + "_" + getCrc(badUserId, testApiKeySecret)
        parseBrokenApiKeyAndAssertThrownMessage(
            apiKeyWithInvalidBase64CharacterInUserId,
            apiKeyUtility.validateKeycloakUserIdExceptionMessage
        )
    }

    @Test
    fun `check if exception thrown if the included api key secret is not in the right format`() {
        val apiKeyWithOneTooManyCharacterInApiKeySecret =
            testApiKeyBase64EncodedKeycloakUserId + "_" +
                testApiKeySecret + "d" + "_" +
                getCrc(testApiKeyBase64EncodedKeycloakUserId, testApiKeySecret + "d")

        parseBrokenApiKeyAndAssertThrownMessage(
            apiKeyWithOneTooManyCharacterInApiKeySecret,
            apiKeyUtility.validateApiKeySecretExceptionMessage
        )
    }

    @Test
    fun `check if exception is thrown if the included CRC32 value is wrong`() {
        val apiKeyWithWrongCrc32Value =
            testApiKeyBase64EncodedKeycloakUserId + "_" + testApiKeySecret + "_" + (testApiKeyCrc32Value + 1L)

        parseBrokenApiKeyAndAssertThrownMessage(
            apiKeyWithWrongCrc32Value, apiKeyUtility.validateApiKeyChecksumWrongValueExceptionMessage
        )
    }

    @Test
    fun `check that generated api keys can be parsed`() {
        val initialParsedApiKey = ParsedApiKey(testKeycloakUserId, testApiKeySecret)
        val apiKey = apiKeyUtility.convertToApiKey(initialParsedApiKey)
        val reParsedApiKey = apiKeyUtility.parseApiKey(apiKey)
        assertEquals(initialParsedApiKey, reParsedApiKey, "expecting that initial and re-parsed api key match")
    }
}
