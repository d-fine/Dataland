package org.dataland.datalandbackendutils.services.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.utils.DataPointUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataPointUtilsTest {
    private val objectMapper = ObjectMapper()
    private val valueFieldName = "value"

    @Test
    fun `check that plain date is converted to extended format with correct field order`() {
        val dateString = "2024-03-22"
        val plainData = objectMapper.writeValueAsString(objectMapper.writeValueAsString(dateString))

        val result = DataPointUtils.convertToExtendedFormat(plainData)

        val deserializedData = objectMapper.readValue(result, String::class.java)
        val jsonNode = objectMapper.readTree(deserializedData)

        val fieldNames = jsonNode.fieldNames().asSequence().toList()
        assertEquals(listOf(valueFieldName), fieldNames)
        assertEquals(dateString, jsonNode.get(valueFieldName).asText())
    }

    @Test
    fun `check that plain enum is converted to extended format with correct field order`() {
        val enumString = "NoDeviation"
        val plainData = objectMapper.writeValueAsString(objectMapper.writeValueAsString(enumString))

        val result = DataPointUtils.convertToExtendedFormat(plainData)

        val deserializedData = objectMapper.readValue(result, String::class.java)
        val jsonNode = objectMapper.readTree(deserializedData)

        val fieldNames = jsonNode.fieldNames().asSequence().toList()
        assertEquals(listOf(valueFieldName), fieldNames)
        assertEquals(enumString, jsonNode.get(valueFieldName).asText())
    }
}
