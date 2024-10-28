package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock

class DataPointManagerTest {
    private val dataPointManager =
        DataPointManager(
            objectMapper = ObjectMapper(),
            dataManager = mock(DataManager::class.java),
            specificationServiceBaseUrl = "/specifications",
        )

    @Test
    fun `test that correct input is not rejected`() {
        val testJson =
            """
            {
                "value": 0.5,
                "currency": "USD"
            }
            """.trimIndent()
        val className = "org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint"
        assertDoesNotThrow { dataPointManager.validateConsistency(testJson, className) }
    }

    @Test
    fun `test that unrecognized properties are rejected`() {
        val testJson =
            """
            {
                "unknownField": 1.5,
                "currency": "USD"
            }
            """.trimIndent()
        val className = "org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint"
        assertThrows<UnrecognizedPropertyException> { dataPointManager.validateConsistency(testJson, className) }
    }

    @Test
    fun `test that invalid inputs are rejected`() {
        val testJson =
            """
            {
                "value": 1.5,
                "currency": "dummy"
            }
            """.trimIndent()
        val className = "org.dataland.datalandbackend.model.datapoints.standard.CurrencyDataPoint"
        assertThrows<IllegalArgumentException> { dataPointManager.validateConsistency(testJson, className) }
    }

    @Test
    fun `test that invalid classes are rejected`() {
        val testJson = "{}"
        val className = "org.dataland.datalandbackend.model.datapoints.standard.DummyDataPoint"
        assertThrows<ClassNotFoundException> { dataPointManager.validateConsistency(testJson, className) }
    }
}
