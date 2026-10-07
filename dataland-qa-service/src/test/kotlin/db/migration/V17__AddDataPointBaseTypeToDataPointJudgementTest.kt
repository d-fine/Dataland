package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

@Suppress("ClassName")
class V17__AddDataPointBaseTypeToDataPointJudgementTest {
    private val migration = V17__AddDataPointBaseTypeToDataPointJudgement()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockStatement = mock<Statement>()
    private val mockTablesResultSet = mock<ResultSet>()
    private val mockColumnsResultSet = mock<ResultSet>()

    private val table = V17__AddDataPointBaseTypeToDataPointJudgement.DATA_POINT_JUDGEMENT_TABLE
    private val column = V17__AddDataPointBaseTypeToDataPointJudgement.DATA_POINT_BASE_TYPE_COLUMN

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockMetaData, mockStatement, mockTablesResultSet, mockColumnsResultSet)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
    }

    private fun stubSchema(
        tableExists: Boolean,
        columnExists: Boolean,
    ) {
        whenever(mockMetaData.getTables(null, null, table, null)).thenReturn(mockTablesResultSet)
        whenever(mockTablesResultSet.next()).thenReturn(tableExists)
        whenever(mockMetaData.getColumns(null, null, table, column)).thenReturn(mockColumnsResultSet)
        whenever(mockColumnsResultSet.next()).thenReturn(columnExists)
    }

    @Test
    fun `migration adds the data point base type column when it does not exist yet`() {
        stubSchema(tableExists = true, columnExists = false)

        migration.migrate(mockContext)

        val sql = argumentCaptor<String>()
        verify(mockStatement).execute(sql.capture())
        assertTrue(sql.firstValue.contains("ALTER TABLE $table"))
        assertTrue(sql.firstValue.contains("ADD COLUMN $column TEXT"))
    }

    @Test
    fun `migration is skipped when the column already exists`() {
        stubSchema(tableExists = true, columnExists = true)

        migration.migrate(mockContext)

        verify(mockConnection, never()).createStatement()
    }

    @Test
    fun `migration is skipped when the table does not exist`() {
        stubSchema(tableExists = false, columnExists = false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).createStatement()
    }
}
