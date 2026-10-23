package org.dataland.datalandbackendutils.services.utils

import org.dataland.datalandbackendutils.utils.DataPointUtils
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataPointUtilsTest {
    private val valueFieldName = "value"

    @Test
    fun `check that plain date is converted to extended format with correct field order`() {
        val dateString = "2024-03-22"
        val plainData = defaultObjectMapper.writeValueAsString(defaultObjectMapper.writeValueAsString(dateString))

        val result = DataPointUtils.convertToExtendedFormat(plainData)

        val deserializedData = defaultObjectMapper.readValue(result, String::class.java)
        val jsonNode = defaultObjectMapper.readTree(deserializedData)

        val fieldNames = jsonNode.fieldNames().asSequence().toList()
        assertEquals(listOf(valueFieldName), fieldNames)
        assertEquals(dateString, jsonNode.get(valueFieldName).asText())
    }

    @Test
    fun `check that plain enum is converted to extended format with correct field order`() {
        val enumString = "NoDeviation"
        val plainData = defaultObjectMapper.writeValueAsString(defaultObjectMapper.writeValueAsString(enumString))

        val result = DataPointUtils.convertToExtendedFormat(plainData)

        val deserializedData = defaultObjectMapper.readValue(result, String::class.java)
        val jsonNode = defaultObjectMapper.readTree(deserializedData)

        val fieldNames = jsonNode.fieldNames().asSequence().toList()
        assertEquals(listOf(valueFieldName), fieldNames)
        assertEquals(enumString, jsonNode.get(valueFieldName).asText())
    }
}
