package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("ClassName")
class V6__UpdateSfdrCurrencyFieldsTest {
    private val migration = V6__UpdateSfdrCurrencyFields()
    private val context = mock<Context>()
    private val connection = mock<Connection>()
    private val resultSet = mock<ResultSet>()
    private val statement = mock<PreparedStatement>()

    @BeforeEach
    fun setup() {
        whenever(context.connection).thenReturn(connection)
        whenever(connection.metaData).thenReturn(mock<java.sql.DatabaseMetaData>())
        whenever(connection.metaData.getTables(null, null, "data_point_meta_information", null)).thenReturn(resultSet)
    }

    @Test
    fun `check that no migration starts if table does not exist`() {
        whenever(resultSet.next()).thenReturn(false)
        migration.migrate(context)
        verify(connection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that migrate calls update for all six types if table exists`() {
        whenever(resultSet.next()).thenReturn(true)
        whenever(connection.prepareStatement(any<String>())).thenReturn(statement)
        whenever(statement.executeUpdate()).thenReturn(1)

        migration.migrate(context)
        verify(connection, times(6)).prepareStatement(
            argThat { input -> input.startsWith("UPDATE") },
        )
        verify(statement, times(6)).executeUpdate()
        verify(statement, times(6)).close()
    }

    @Test
    fun `check that extendedCurrencyTotalRevenue is updated correctly`() {
        whenever(connection.prepareStatement(any<String>())).thenReturn(statement)
        whenever(statement.executeUpdate()).thenReturn(2)

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
        whenever(connection.prepareStatement(any<String>())).thenReturn(statement)
        whenever(statement.executeUpdate()).thenReturn(2)

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
        whenever(connection.prepareStatement(any<String>())).thenReturn(statement)
        whenever(statement.executeUpdate()).thenReturn(2)

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
