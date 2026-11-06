package org.dataland.datalandcompanyservice.configurations

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class AzureOpenAiConfigurationValidatorTest {
    @Test
    fun `validator accepts complete configuration`() {
        assertDoesNotThrow { AzureOpenAiConfigurationValidator("https://example.openai.azure.com", "key", "deployment") }
    }

    @Test
    fun `validator fails and names the missing settings`() {
        val exception = assertThrows<IllegalStateException> { AzureOpenAiConfigurationValidator("https://e", " ", "") }
        assertTrue(exception.message!!.contains("AZURE_OPENAI_API_KEY"))
        assertTrue(exception.message!!.contains("AZURE_OPENAI_DEPLOYMENT_NAME"))
        assertTrue(!exception.message!!.contains("AZURE_OPENAI_ENDPOINT"))
    }
}
