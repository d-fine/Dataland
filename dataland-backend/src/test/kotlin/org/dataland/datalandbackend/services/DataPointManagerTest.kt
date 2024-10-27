package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class DataPointManagerTest {
    val dataPointManager = DataPointManager(specificationServiceBaseUrl = "http://localhost:8081/specifications")

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
        assertDoesNotThrow { dataPointManager.validateDatapoint(testJson, className) }
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
        assertThrows<UnrecognizedPropertyException> { dataPointManager.validateDatapoint(testJson, className) }
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
        assertThrows<IllegalArgumentException> { dataPointManager.validateDatapoint(testJson, className) }
    }

    @Test
    fun `test that invalid classes are rejected`() {
        val testJson = "{}"
        val className = "org.dataland.datalandbackend.model.datapoints.standard.DummyDataPoint"
        assertThrows<ClassNotFoundException> { dataPointManager.validateDatapoint(testJson, className) }
    }

    @Test
    fun `try out`() {
        // ToDo: Implement this test properly (option for mocking the specification service: https://wiremock.org/index.html)
        val testJson =
            """
            {
                "value": 0.5,
                "currency": "UD"
            }
            """.trimIndent()
        val validTestUrl = "http://localhost:8081/specifications/datatypes/datapoint-with-source-bigdecimal.json"
        val invalidTestUrl = "http://localhost:8081/specifications/datatypes/datapoint-with-source-bigdecimal-invalid.json"
        val node = dataPointManager.getJsonNodeFromUrl(validTestUrl)
        println(node)
        println(node.get("validatedBy"))
        // dataPointManager.validateDatapoint(testJson, node.get("validatedBy").textValue())
        // val node2 = dataPointManager.getJsonNodeFromUrl(invalidTestUrl)
        // println(node2)
    }
}
