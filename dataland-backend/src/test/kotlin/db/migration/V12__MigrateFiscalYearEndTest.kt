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
class V12__MigrateFiscalYearEndTest {
    private val migration = V12__MigrateFiscalYearEnd()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<java.sql.DatabaseMetaData>()
    private val mockTableResultSet = mock<ResultSet>()
    private val mockColumnResultSet = mock<ResultSet>()
    private val mockSelectStatement = mock<PreparedStatement>()
    private val mockUpdateStatement = mock<PreparedStatement>()
    private val mockSelectResultSet = mock<ResultSet>()

    @BeforeEach
    fun setup() {
        reset(
            mockContext, mockConnection, mockMetaData, mockTableResultSet,
            mockColumnResultSet, mockSelectStatement, mockUpdateStatement, mockSelectResultSet
        )
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(mockMetaData.getTables(null, null, "stored_companies", null)).thenReturn(mockTableResultSet)
        whenever(mockMetaData.getColumns(null, null, "stored_companies", "fiscal_year_end")).thenReturn(mockColumnResultSet)
    }

    @Test
    fun `does nothing if table does not exist`() {
        whenever(mockTableResultSet.next()).thenReturn(false)
        migration.migrate(mockContext)
        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `does nothing if column does not exist`() {
        whenever(mockTableResultSet.next()).thenReturn(true)
        whenever(mockColumnResultSet.next()).thenReturn(false)
        migration.migrate(mockContext)
        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `migrates fiscal_year_end values if table and column exist`() {
        whenever(mockTableResultSet.next()).thenReturn(true)
        whenever(mockColumnResultSet.next()).thenReturn(true)
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockSelectStatement, mockUpdateStatement)
        whenever(mockSelectStatement.executeQuery()).thenReturn(mockSelectResultSet)
        whenever(mockSelectResultSet.next()).thenReturn(true, true, false)
        whenever(mockSelectResultSet.getString("company_id")).thenReturn("id1", "id2")
        whenever(mockSelectResultSet.getString("fiscal_year_end")).thenReturn("2023-12-31", "2024-03-31")

        migration.migrate(mockContext)

        verify(mockConnection).prepareStatement(
            "ALTER TABLE stored_companies\nALTER COLUMN fiscal_year_end TYPE VARCHAR(10);"
        )
        verify(mockConnection).prepareStatement("SELECT company_id, fiscal_year_end FROM stored_companies")
        verify(mockConnection).prepareStatement("UPDATE stored_companies SET fiscal_year_end = ? WHERE company_id = ?")
        verify(mockUpdateStatement).setString(1, argThat { value -> value == "31-Dec" })
        verify(mockUpdateStatement).setString(2, "id1")
        verify(mockUpdateStatement).setString(1, argThat { value -> value == "31-Mar" })
        verify(mockUpdateStatement).setString(2, "id2")
        verify(mockUpdateStatement, times(2)).executeUpdate()
        verify(mockSelectResultSet).close()
        verify(mockSelectStatement).close()
        verify(mockUpdateStatement).close()
    }
}
