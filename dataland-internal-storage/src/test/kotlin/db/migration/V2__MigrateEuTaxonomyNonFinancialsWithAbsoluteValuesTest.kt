package db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

class V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValuesTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `test that migration writes the expected results into the datatable`() {
        val originalDatabaseDataEntry = buildOriginalDatabaseEntry()
        val expectedTransformedDatabaseDataEntry = buildExpectedTransformedDatabaseEntry()
        val migration = V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues()
        val mockContext = mock(Context::class.java)
        val mockConnection = mock(Connection::class.java)
        val mockStatement = mock(Statement::class.java)
        val mockResultSet = mock(ResultSet::class.java)
        `when`(mockResultSet.getString("data_id")).thenReturn("data-id")
        `when`(mockResultSet.getString("data")).thenReturn(originalDatabaseDataEntry)
        `when`(mockResultSet.next()).thenReturn(true, false)
        `when`(mockStatement.executeQuery(any())).thenReturn(mockResultSet)
        `when`(mockStatement.execute(any())).then {
            val databaseUpdateQuery = it.arguments[0] as String
            val newDatabaseEntryString = databaseUpdateQuery.split("'")[1]
            val newDatabaseEntry = JSONObject(objectMapper.readValue(newDatabaseEntryString, String::class.java))
            assertTrue(
                JSONObject(objectMapper.readValue(expectedTransformedDatabaseDataEntry, String::class.java)).similar(
                    newDatabaseEntry,
                ),
            )
            return@then false
        }
        `when`(mockConnection.createStatement()).thenReturn(mockStatement)
        `when`(mockContext.connection).thenReturn(mockConnection)
        migration.migrate(mockContext)
    }

    val affectedFields = listOf("capex", "opex", "revenue")
    val unaffectedFields = listOf("something")
    private fun buildOriginalDatabaseEntry(): String {
        val dataset = JSONObject()
        (affectedFields + unaffectedFields).forEach {
            dataset.put(it, buildOldDetailsPerCashFlowType())
        }
        return buildDatabaseEntry(dataset)
    }
    private fun buildExpectedTransformedDatabaseEntry(): String {
        val dataset = JSONObject()
        (affectedFields).forEach {
            dataset.put(it, buildNewDetailsPerCashFlowType())
        }
        (unaffectedFields).forEach {
            dataset.put(it, buildOldDetailsPerCashFlowType())
        }
        return buildDatabaseEntry(dataset)
    }

    private fun buildDatabaseEntry(dataset: JSONObject): String {
        val dataBaseEntry = JSONObject()
        dataBaseEntry.put("dataType", "eutaxonomy-non-financials")
        dataBaseEntry.put("data", dataset.toString())
        return objectMapper.writeValueAsString(dataBaseEntry.toString())
    }

    private val unaffectedDetail = "totalAmount"
    private fun buildOldDetailsPerCashFlowType(): JSONObject {
        val cashFlowObject = JSONObject()
        listOf(unaffectedDetail, "alignedPercentage", "eligiblePercentage").forEach {
            cashFlowObject.put(it, dummyDataPoint)
        }
        return cashFlowObject
    }

    private val dummyDataPoint = JSONObject(
        "{\"value\":0.1,\"dataSource\":{\"report\":\"some report\"},\"quality\":\"Estimated\"}",
    )
    private val dummyDataPointAbsoluteAndPercentage = JSONObject(
        "{\"valueAsPercentage\":0.1,\"dataSource\":{\"report\":\"some report\"},\"quality\":\"Estimated\"}",
    )
    private fun buildNewDetailsPerCashFlowType(): JSONObject {
        val cashFlowObject = JSONObject()
        listOf("alignedData", "eligibleData").forEach {
            cashFlowObject.put(
                it, dummyDataPointAbsoluteAndPercentage,
            )
        }
        cashFlowObject.put(unaffectedDetail, dummyDataPoint)
        return cashFlowObject
    }
}
