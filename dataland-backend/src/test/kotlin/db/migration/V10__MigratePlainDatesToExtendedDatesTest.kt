package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
class V10__MigratePlainDatesToExtendedDatesTest {
    private val migration = V10__MigratePlainDatesToExtendedDates()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockResultSet = mock<ResultSet>()
    private val mockPreparedStatement = mock<PreparedStatement>()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mock<java.sql.DatabaseMetaData>())
        whenever(
            mockConnection.metaData.getTables(null, null, "data_point_meta_information", null),
        ).thenReturn(mockResultSet)
        whenever(mockConnection.metaData.getTables(null, null, "data_point_uuid_map", null)).thenReturn(mockResultSet)
    }

    @Test
    fun `check that no migration starts if table does not exist`() {
        whenever(mockResultSet.next()).thenReturn(false)
        migration.migrate(mockContext)
        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that migrate processes both data point types for both tables`() {
        whenever(mockResultSet.next()).thenReturn(true)
        val mockConflictResultSet = mock<ResultSet>()
        whenever(mockConflictResultSet.next()).thenReturn(false)
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeQuery()).thenReturn(mockConflictResultSet)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(0)

        migration.migrate(mockContext)

        verify(mockPreparedStatement, times(8)).executeUpdate()
    }

    @Test
    fun `check that conflicting tuples are found correctly`() {
        val mockConflictResultSet = mock<ResultSet>()
        whenever(mockConflictResultSet.next()).thenReturn(true, true, false)
        whenever(mockConflictResultSet.getString("company_id")).thenReturn("company-1", "company-2")
        whenever(mockConflictResultSet.getString("reporting_period")).thenReturn("2024", "2023")

        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeQuery()).thenReturn(mockConflictResultSet)

        val conflicts =
            migration.findConflictingTuples(
                mockContext,
                "data_point_meta_information",
                "data_point_type",
                "plainDateFiscalYearEnd",
                "extendedDateFiscalYearEnd",
            )

        assertEquals(2, conflicts.size)
        assertTrue(
            conflicts.contains(
                V10__MigratePlainDatesToExtendedDates.DataPointTuple("company-1", "2024"),
            ),
        )
        assertTrue(
            conflicts.contains(
                V10__MigratePlainDatesToExtendedDates.DataPointTuple("company-2", "2023"),
            ),
        )
    }

    @Test
    fun `check that conflicting plain data points are deactivated`() {
        val conflicts =
            setOf(
                V10__MigratePlainDatesToExtendedDates.DataPointTuple("company-1", "2024"),
                V10__MigratePlainDatesToExtendedDates.DataPointTuple("company-2", "2023"),
            )

        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(1)

        val deactivatedCount =
            migration.deactivateConflictingPlainDataPoints(
                mockContext,
                "data_point_meta_information",
                "data_point_type",
                "plainDateFiscalYearEnd",
                conflicts,
            )

        assertEquals(2, deactivatedCount)
        verify(mockPreparedStatement, times(2)).executeUpdate()
        verify(mockPreparedStatement).close()
    }

    @Test
    fun `check that plain data points are updated to extended format`() {
        val mockConflictResultSet = mock<ResultSet>()
        whenever(mockConflictResultSet.next()).thenReturn(false)
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeQuery()).thenReturn(mockConflictResultSet)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(5)

        migration.migratePlainToExtended(
            mockContext,
            "data_point_meta_information",
            "data_point_type",
            "plainDateFiscalYearEnd",
            "extendedDateFiscalYearEnd",
        )

        verify(mockPreparedStatement, times(1)).executeUpdate()
        verify(mockPreparedStatement).setString(1, "extendedDateFiscalYearEnd")
        verify(mockPreparedStatement).setString(2, "plainDateFiscalYearEnd")
    }

    @Test
    fun `check DataPointTuple equality`() {
        val tuple1 = V10__MigratePlainDatesToExtendedDates.DataPointTuple("company-1", "2024")
        val tuple2 = V10__MigratePlainDatesToExtendedDates.DataPointTuple("company-1", "2024")
        val tuple3 = V10__MigratePlainDatesToExtendedDates.DataPointTuple("company-2", "2024")

        assertEquals(tuple1, tuple2)
        assertEquals(tuple1.hashCode(), tuple2.hashCode())
        assertTrue(tuple1 != tuple3)
    }
}
