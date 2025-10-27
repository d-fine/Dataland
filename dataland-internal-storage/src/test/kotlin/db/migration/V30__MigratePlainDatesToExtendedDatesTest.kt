package db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V30__MigratePlainDatesToExtendedDatesTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `check that plain date is converted to extended format with correct field order`() {
        val migration = V30__MigratePlainDatesToExtendedDates()
        val plainData = objectMapper.writeValueAsString(objectMapper.writeValueAsString("2024-03-22"))

        val result = migration.convertToExtendedFormat(plainData)

        val deserializedData = objectMapper.readValue(result, String::class.java)
        val jsonNode = objectMapper.readTree(deserializedData)

        val fieldNames = jsonNode.fieldNames().asSequence().toList()
        assertEquals(listOf("value"), fieldNames)
        assertEquals("2024-03-22", jsonNode.get("value").asText())
    }

    @Test
    fun `check that plain enum is converted to extended format with correct field order`() {
        val migration = V30__MigratePlainDatesToExtendedDates()
        val plainData = objectMapper.writeValueAsString(objectMapper.writeValueAsString("NoDeviation"))

        val result = migration.convertToExtendedFormat(plainData)

        val deserializedData = objectMapper.readValue(result, String::class.java)
        val jsonNode = objectMapper.readTree(deserializedData)

        val fieldNames = jsonNode.fieldNames().asSequence().toList()
        assertEquals(listOf("value"), fieldNames)
        assertEquals("NoDeviation", jsonNode.get("value").asText())
    }
}
