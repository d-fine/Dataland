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
    fun `check that no migration starts if table does not exist`() {
        `when`(resultSet.next()).thenReturn(false)
        migration.migrate(context)
        verify(connection, never()).prepareStatement(anyString())
    }

    @Test
    fun `check that migrate calls update for all six types if table exists`() {
        `when`(resultSet.next()).thenReturn(true)
        `when`(connection.prepareStatement(anyString())).thenReturn(statement)
        `when`(statement.executeUpdate()).thenReturn(1)

        migration.migrate(context)
        verify(connection, times(6)).prepareStatement(
            startsWith("UPDATE "),
        )
        verify(statement, times(6)).executeUpdate()
        verify(statement, times(6)).close()
    }

    @Test
    fun `check that extendedCurrencyTotalRevenue is updated correctly`() {
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
    fun `check that extendedCurrencyEnterpriseValue is updated correctly`() {
        `when`(connection.prepareStatement(anyString())).thenReturn(statement)
        `when`(statement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            context = context,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            dataPointType = "extendedCurrencyEnterpriseValue",
        )

        verify(statement).setString(1, "extendedDecimalEnterpriseValueInEUR")
        verify(statement).setString(2, "extendedCurrencyEnterpriseValue")
        verify(statement).executeUpdate()
        verify(statement).close()
    }

    @Test
    fun `check that extendedDecimalCarbonFootprint is updated correctly`() {
        `when`(connection.prepareStatement(anyString())).thenReturn(statement)
        `when`(statement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            context = context,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            dataPointType = "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
        )

        verify(statement).setString(
            1,
            "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
        )
        verify(statement).setString(
            2,
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
        )
        verify(statement).executeUpdate()
        verify(statement).close()
    }

    @Test
    fun `check that migrateBackendTable throws exception for unknown type`() {
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
