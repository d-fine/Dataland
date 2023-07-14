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
        val mockContext = mock(Context::class.java)
        val mockConnection = mock(Connection::class.java)
        val mockStatement = mock(Statement::class.java)
        val mockResultSet = mock(ResultSet::class.java)
        `when`(mockResultSet.getString("data_id")).thenReturn("data-id")
        `when`(mockResultSet.getString("data")).thenReturn(buildOriginalDatabaseEntry())
        `when`(mockResultSet.next()).thenReturn(true, false)
        `when`(mockStatement.executeQuery(any())).thenReturn(mockResultSet)
        `when`(mockStatement.execute(any())).then {
            val databaseUpdateQuery = it.arguments[0] as String
            val newDatabaseEntryString = databaseUpdateQuery.split("'")[1]
            val newDatabaseEntry = JSONObject(objectMapper.readValue(newDatabaseEntryString, String::class.java))
            assertTrue(
                JSONObject(objectMapper.readValue(buildExpectedTransformedDatabaseEntry(), String::class.java)).similar(
                    newDatabaseEntry,
                ),
            )
            return@then false
        }
        `when`(mockConnection.createStatement()).thenReturn(mockStatement)
        `when`(mockContext.connection).thenReturn(mockConnection)
        val migration = V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues()
        migration.migrate(mockContext)
    }

    private val affectedFields = listOf("capex", "opex", "revenue")
    private val unaffectedFields = listOf("something")
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
    private val dummyDataPoint = JSONObject(
        "{\"value\":0.1,\"dataSource\":{\"report\":\"some report\"},\"quality\":\"Estimated\"}",
    )
    private val dummyDataPointAbsoluteAndPercentage = JSONObject(
        "{\"valueAsPercentage\":0.1,\"dataSource\":{\"report\":\"some report\"},\"quality\":\"Estimated\"}",
    )
    private fun buildOldDetailsPerCashFlowType(): JSONObject {
        val cashFlowObject = JSONObject()
        listOf(unaffectedDetail, "alignedPercentage").forEach {
            cashFlowObject.put(it, dummyDataPoint)
        }
        cashFlowObject.put("eligiblePercentage", JSONObject.NULL);
        return cashFlowObject
    }

    private fun buildNewDetailsPerCashFlowType(): JSONObject {
        val cashFlowObject = JSONObject()
        cashFlowObject.put("alignedData", dummyDataPointAbsoluteAndPercentage,)
        cashFlowObject.put("eligibleData", JSONObject.NULL)
        cashFlowObject.put(unaffectedDetail, dummyDataPoint)
        return cashFlowObject
    }
}
