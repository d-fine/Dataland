package org.dataland.datalandbackend.services

import org.dataland.datalandbackendutils.apikey.ApiKeyPrevalidator
import org.dataland.datalandbackendutils.apikey.ParsedApiKey
import org.dataland.datalandbackendutils.exceptions.ApiKeyFormatException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ApiKeyPrevalidatorTest {

    private val apiKeyPrevalidator = ApiKeyPrevalidator()

    private val testApiKeyKeycloakUserId = "YzVlZjEwYjEtZGUyMy00YTAxLTkwMDUtZTYyZWEyMjZlZTgz"
    private val testApiKeySecret = "da030a8c290f43a022bdb3da59f6ee4d7b5e290997fbdb3a22f1a7ce5a1a51c87f7666a3476a950b"
    private val testApiKeyCrc32Value = "465063745"
    private val testApiKeyWithoutCrc32Value = testApiKeyKeycloakUserId + "_" + testApiKeySecret

    private fun addOrSubtractOneFromNumberInsideCrc32Range(crc32Value: Long): Long {
        if (crc32Value !in ApiKeyPrevalidator.minPossibleCrc32Value..ApiKeyPrevalidator.maxPossibleCrc32Value) {
            throw IllegalArgumentException(
                "This util method only accepts numbers which are in the valid range " +
                    "of CRC32 values."
            )
        }
        return when (crc32Value) {
            ApiKeyPrevalidator.maxPossibleCrc32Value -> crc32Value - 1
            else -> crc32Value + 1
        }
    }

    private fun buildTestApiKeyAndOptionallyReplaceWithCustomInputs(
        keycloakUserId: String = testApiKeyKeycloakUserId,
        apiKeySecret: String = testApiKeySecret,
        crc32Value: String = testApiKeyCrc32Value
    ): String {
        return keycloakUserId + "_" + apiKeySecret + "_" + crc32Value
    }

    private fun prevalidateBrokenApiKeysAndAssertThrownMessages(
        mapOfBrokenApiKeysAndExpectedMessages:
            Map<String, String>
    ) {
        mapOfBrokenApiKeysAndExpectedMessages.forEach { (brokenApiKey, expectedMessage) ->
            val thrown = assertThrows<ApiKeyFormatException> {
                apiKeyPrevalidator.prevalidateApiKey(brokenApiKey)
            }
            assertEquals(
                expectedMessage,
                thrown.message
            )
        }
    }

    @Test
    fun `Check if prevalidation passes for a correct api key`() {
        val parsedApiKey = apiKeyPrevalidator.prevalidateApiKey(buildTestApiKeyAndOptionallyReplaceWithCustomInputs())
        val expectedParsedApiKey = ParsedApiKey(
            testApiKeyKeycloakUserId, testApiKeySecret,
            testApiKeyCrc32Value, testApiKeyWithoutCrc32Value
        )
        assertEquals(expectedParsedApiKey, parsedApiKey)
    }

    @Test
    fun `Check if exception thrown if the provided api key does not have the exact number of delimiters`() {
        val apiKeyWithOneTooManyDelimiter = buildTestApiKeyAndOptionallyReplaceWithCustomInputs() + "_"
        val totallyRandomString = "aksjflakjsglkajsglkjas"
        val expectedApiKeyFormatExceptionMessage = apiKeyPrevalidator.validateApiKeyDelimitersExceptionMessage
        prevalidateBrokenApiKeysAndAssertThrownMessages(
            mapOf(
                apiKeyWithOneTooManyDelimiter to expectedApiKeyFormatExceptionMessage,
                totallyRandomString to expectedApiKeyFormatExceptionMessage
            )
        )
    }

    @Test
    fun `Check if exception thrown if the included keycloak user Id is not in Base64 format`() {
        val apiKeyWithInvalidBase64CharacterInUserId = ")" + buildTestApiKeyAndOptionallyReplaceWithCustomInputs()
        prevalidateBrokenApiKeysAndAssertThrownMessages(
            mapOf(apiKeyWithInvalidBase64CharacterInUserId to apiKeyPrevalidator.validateKeycloakUserIdExceptionMessage)
        )
    }

    @Test
    fun `Check if exception thrown if the included api key secret is not in Hex format or has the wrong length`() {
        val apiKeySecretWithInvalidHexCharacter = testApiKeySecret.replaceFirstChar { "z" }
        val apiKeySecretWithOneTooManyCharacter = testApiKeySecret + "1"

        val apiKeyWithInvalidHexCharacterInApiKeySecret = buildTestApiKeyAndOptionallyReplaceWithCustomInputs(
            apiKeySecret = apiKeySecretWithInvalidHexCharacter
        )
        val apiKeyWithOneTooManyCharacterInApiKeySecret = buildTestApiKeyAndOptionallyReplaceWithCustomInputs(
            apiKeySecret = apiKeySecretWithOneTooManyCharacter
        )

        val expectedApiKeyFormatExceptionMessage = apiKeyPrevalidator.validateApiKeySecretExceptionMessage
        prevalidateBrokenApiKeysAndAssertThrownMessages(
            mapOf(
                apiKeyWithInvalidHexCharacterInApiKeySecret to expectedApiKeyFormatExceptionMessage,
                apiKeyWithOneTooManyCharacterInApiKeySecret to expectedApiKeyFormatExceptionMessage
            )
        )
    }

    @Test
    fun `Check if exception thrown if the included CRC32 value is not a valid number or is an out of range number`() {
        val apiKeyWithInvalidFormatForCrc32Value = buildTestApiKeyAndOptionallyReplaceWithCustomInputs() + "a"
        val apiKeyWithLeadingZeroNumberInCrc32Value =
            buildTestApiKeyAndOptionallyReplaceWithCustomInputs(crc32Value = "022")
        val expectedApiKeyFormatExceptionMessageInvalidCrc32Format =
            apiKeyPrevalidator.validateCrc32ValueNoValidNumberExceptionMessage

        val apiKeyWithOutOfRangeCrc32Value = buildTestApiKeyAndOptionallyReplaceWithCustomInputs(
            crc32Value = (ApiKeyPrevalidator.maxPossibleCrc32Value + 1).toString()
        )
        val expectedApiKeyFormatExceptionMessageOutOfRangeNumber =
            apiKeyPrevalidator.validateCrc32ValueOutOfRangeExceptionMessage

        prevalidateBrokenApiKeysAndAssertThrownMessages(
            mapOf(
                apiKeyWithInvalidFormatForCrc32Value to expectedApiKeyFormatExceptionMessageInvalidCrc32Format,
                apiKeyWithLeadingZeroNumberInCrc32Value to expectedApiKeyFormatExceptionMessageInvalidCrc32Format,
                apiKeyWithOutOfRangeCrc32Value to expectedApiKeyFormatExceptionMessageOutOfRangeNumber
            )
        )
    }

    @Test
    fun `Check if exception is thrown if the included CRC32 value has the right format but is just wrong`() {
        val apiKeyWrongCrc32Value = addOrSubtractOneFromNumberInsideCrc32Range(testApiKeyCrc32Value.toLong()).toString()
        val apiKeyWithWrongCrc32Value =
            buildTestApiKeyAndOptionallyReplaceWithCustomInputs(crc32Value = apiKeyWrongCrc32Value)

        val thrown = assertThrows<IllegalArgumentException> {
            apiKeyPrevalidator.prevalidateApiKey(apiKeyWithWrongCrc32Value)
        }
        assertEquals(
            apiKeyPrevalidator.validateApiKeyChecksumWrongValueExceptionMessage,
            thrown.message
        )
    }
}
