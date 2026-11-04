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
class V21__MakeDataPointIdNullableInDataPointJudgementTest {
    private val migration = V21__MakeDataPointIdNullableInDataPointJudgement()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockStatement = mock<Statement>()
    private val mockColumnsResultSet = mock<ResultSet>()

    private val table = V21__MakeDataPointIdNullableInDataPointJudgement.DATA_POINT_JUDGEMENT_TABLE
    private val column = V21__MakeDataPointIdNullableInDataPointJudgement.DATA_POINT_ID_COLUMN

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockMetaData, mockStatement, mockColumnsResultSet)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
    }

    private fun stubColumn(
        columnExists: Boolean,
        nullable: Int = DatabaseMetaData.columnNoNulls,
    ) {
        whenever(mockMetaData.getColumns(null, null, table, column)).thenReturn(mockColumnsResultSet)
        whenever(mockColumnsResultSet.next()).thenReturn(columnExists)
        whenever(mockColumnsResultSet.getInt("NULLABLE")).thenReturn(nullable)
    }

    @Test
    fun `migration drops the not null constraint when the column is not nullable`() {
        stubColumn(columnExists = true, nullable = DatabaseMetaData.columnNoNulls)

        migration.migrate(mockContext)

        val sql = argumentCaptor<String>()
        verify(mockStatement).execute(sql.capture())
        assertTrue(sql.firstValue.contains("ALTER TABLE $table"))
        assertTrue(sql.firstValue.contains("ALTER COLUMN $column DROP NOT NULL"))
    }

    @Test
    fun `migration is skipped when the column is already nullable`() {
        stubColumn(columnExists = true, nullable = DatabaseMetaData.columnNullable)

        migration.migrate(mockContext)

        verify(mockConnection, never()).createStatement()
    }

    @Test
    fun `migration is skipped when the table or column does not exist`() {
        stubColumn(columnExists = false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).createStatement()
    }
}
