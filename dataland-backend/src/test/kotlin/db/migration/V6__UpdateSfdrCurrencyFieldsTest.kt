package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("ClassName")
class V6__UpdateSfdrCurrencyFieldsTest {
    private val migration = V6__UpdateSfdrCurrencyFields()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockResultSet = mock<ResultSet>()
    private val mockStatement = mock<PreparedStatement>()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockResultSet, mockStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mock<java.sql.DatabaseMetaData>())
        whenever(mockConnection.metaData.getTables(null, null, "data_point_meta_information", null)).thenReturn(mockResultSet)
    }

    @Test
    fun `check that no migration starts if table does not exist`() {
        whenever(mockResultSet.next()).thenReturn(false)
        migration.migrate(mockContext)
        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that migrate calls update for all six types if table exists`() {
        whenever(mockResultSet.next()).thenReturn(true)
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate()).thenReturn(1)

        migration.migrate(mockContext)
        verify(mockConnection, times(6)).prepareStatement(
            argThat { input -> input.startsWith("UPDATE") },
        )
        verify(mockStatement, times(6)).executeUpdate()
        verify(mockStatement, times(6)).close()
    }

    @Test
    fun `check that extendedCurrencyTotalRevenue is updated correctly`() {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            dataPointType = "extendedCurrencyTotalRevenue",
        )

        verify(mockStatement).setString(1, "extendedDecimalTotalRevenueInEUR")
        verify(mockStatement).setString(2, "extendedCurrencyTotalRevenue")
        verify(mockStatement).executeUpdate()
        verify(mockStatement).close()
    }

    @Test
    fun `check that extendedCurrencyEnterpriseValue is updated correctly`() {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            dataPointType = "extendedCurrencyEnterpriseValue",
        )

        verify(mockStatement).setString(1, "extendedDecimalEnterpriseValueInEUR")
        verify(mockStatement).setString(2, "extendedCurrencyEnterpriseValue")
        verify(mockStatement).executeUpdate()
        verify(mockStatement).close()
    }

    @Test
    fun `check that extendedDecimalCarbonFootprint is updated correctly`() {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate()).thenReturn(2)

        migration.migrateBackendTable(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            dataPointType = "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
        )

        verify(mockStatement).setString(
            1,
            "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
        )
        verify(mockStatement).setString(
            2,
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
        )
        verify(mockStatement).executeUpdate()
        verify(mockStatement).close()
    }

    @Test
    fun `check that migrateBackendTable throws exception for unknown type`() {
        val ex =
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                migration.migrateBackendTable(
                    context = mockContext,
                    tableName = "data_point_meta_information",
                    columnName = "data_point_type",
                    dataPointType = "unknownType",
                )
            }
        assert(ex.message!!.contains("No renaming defined"))
    }
}
