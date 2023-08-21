package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

fun mockAndWhenConfigurationForFrameworkMigration(
    context: Context,
    originalDatabaseEntry: String,
    expectedTransformedDatabaseEntry: String,
) {
    val objectMapper = ObjectMapper()
    val mockConnection = Mockito.mock(Connection::class.java)
    val mockStatement = Mockito.mock(Statement::class.java)
    val mockResultSet = Mockito.mock(ResultSet::class.java)
    Mockito.`when`(mockResultSet.getString("data_id")).thenReturn("data-id")
    Mockito.`when`(mockResultSet.getString("data")).thenReturn(originalDatabaseEntry)
    Mockito.`when`(mockResultSet.next()).thenReturn(true, false, true, false)
    Mockito.`when`(mockStatement.executeQuery(ArgumentMatchers.any())).thenReturn(mockResultSet)
    Mockito.`when`(mockStatement.execute(ArgumentMatchers.any())).then {
        val databaseUpdateQuery = it.arguments[0] as String
        val newDatabaseEntryString = databaseUpdateQuery.split("'")[1] // TODO this is problematic
        val actualCompanyAssociatedData = JSONObject(objectMapper.readValue(newDatabaseEntryString, String::class.java))
        val expectedCompanyAssociatedData = JSONObject(objectMapper.readValue(expectedTransformedDatabaseEntry, String::class.java))
        Assertions.assertTrue(
                JSONObject(expectedCompanyAssociatedData.getString("data")).similar(
                        JSONObject(actualCompanyAssociatedData.getString("data")),
                ),
        )
        expectedCompanyAssociatedData.remove("data")
        actualCompanyAssociatedData.remove("data")
        Assertions.assertTrue(
            expectedCompanyAssociatedData.similar(
                actualCompanyAssociatedData,
            ),
        )
        return@then false
    }
    Mockito.`when`(mockConnection.createStatement()).thenReturn(mockStatement)
    Mockito.`when`(context.connection).thenReturn(mockConnection)
}
