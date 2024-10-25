package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class DataPointManagerTest {
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
        assertDoesNotThrow { DataPointManager().validateDatapoint(testJson, className) }
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
        assertThrows<UnrecognizedPropertyException> { DataPointManager().validateDatapoint(testJson, className) }
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
        assertThrows<IllegalArgumentException> { DataPointManager().validateDatapoint(testJson, className) }
    }

    @Test
    fun `test that invalid classes are rejected`() {
        val testJson = "{}"
        val className = "org.dataland.datalandbackend.model.datapoints.standard.DummyDataPoint"
        assertThrows<ClassNotFoundException> { DataPointManager().validateDatapoint(testJson, className) }
    }
}
