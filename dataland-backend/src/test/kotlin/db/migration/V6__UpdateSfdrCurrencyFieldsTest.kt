@file:Suppress("ktlint:standard:no-wildcard-imports")

package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.startsWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("ClassName")
class V6__UpdateSfdrCurrencyFieldsTest {
    private lateinit var migration: V6__UpdateSfdrCurrencyFields
    private lateinit var context: Context
    private lateinit var connection: Connection
    private lateinit var resultSet: ResultSet
    private lateinit var statement: PreparedStatement

    @BeforeEach
    fun setup() {
        migration = V6__UpdateSfdrCurrencyFields()
        context = mock(Context::class.java)
        connection = mock(Connection::class.java)
        resultSet = mock(ResultSet::class.java)
        statement = mock(PreparedStatement::class.java)

        `when`(context.connection).thenReturn(connection)
        `when`(connection.metaData).thenReturn(mock(java.sql.DatabaseMetaData::class.java))
        `when`(connection.metaData.getTables(null, null, "data_point_meta_information", null)).thenReturn(resultSet)
    }

    @Test
    fun `test migrate does not run if table does not exist`() {
        `when`(resultSet.next()).thenReturn(false)

        migration.migrate(context)

        // No interactions should happen if the table doesn't exist
        verify(connection, never()).prepareStatement(anyString())
    }

    @Test
    fun `test migrate calls update for all six types if table exists`() {
        `when`(resultSet.next()).thenReturn(true)
        `when`(connection.prepareStatement(anyString())).thenReturn(statement)
        `when`(statement.executeUpdate()).thenReturn(1)

        migration.migrate(context)

        // Should be called 6 times (3 types × 2 tables)
        verify(connection, times(6)).prepareStatement(
            startsWith("UPDATE "),
        )
        verify(statement, times(6)).executeUpdate()
        verify(statement, times(6)).close()
    }

    @Test
    fun `test migrateBackendTable performs update with correct parameters`() {
        `when`(connection.prepareStatement(anyString())).thenReturn(statement)
        `when`(statement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            context = context,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            dataPointType = "extendedCurrencyTotalRevenue",
        )

        verify(statement).setString(1, "extendedDecimalTotalRevenueInEUR")
        verify(statement).setString(2, "extendedCurrencyTotalRevenue")
        verify(statement).executeUpdate()
        verify(statement).close()
    }

    @Test
    fun `test migrateBackendTable throws exception for unknown type`() {
        val ex =
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                migration.migrateBackendTable(
                    context = context,
                    tableName = "data_point_meta_information",
                    columnName = "data_point_type",
                    dataPointType = "unknownType",
                )
            }

        assert(ex.message!!.contains("No renaming defined"))
    }
}
