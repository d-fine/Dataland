package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertEquals
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
class V20__RenameReasonForCustomDataPointToReviewerCommentTest {
    private val migration = V20__RenameReasonForCustomDataPointToReviewerComment()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockStatement = mock<Statement>()
    private val mockTablesResultSet = mock<ResultSet>()
    private val mockOldColumnResultSet = mock<ResultSet>()
    private val mockNewColumnResultSet = mock<ResultSet>()

    private val table = V20__RenameReasonForCustomDataPointToReviewerComment.DATA_POINT_JUDGEMENT_TABLE
    private val oldColumn = V20__RenameReasonForCustomDataPointToReviewerComment.OLD_COLUMN
    private val newColumn = V20__RenameReasonForCustomDataPointToReviewerComment.NEW_COLUMN

    @BeforeEach
    fun setup() {
        reset(
            mockContext, mockConnection, mockMetaData, mockStatement,
            mockTablesResultSet, mockOldColumnResultSet, mockNewColumnResultSet,
        )
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
    }

    private fun stubSchema(
        tableExists: Boolean,
        oldColumnExists: Boolean,
        newColumnExists: Boolean,
    ) {
        whenever(mockMetaData.getTables(null, null, table, null)).thenReturn(mockTablesResultSet)
        whenever(mockTablesResultSet.next()).thenReturn(tableExists)
        whenever(mockMetaData.getColumns(null, null, table, oldColumn)).thenReturn(mockOldColumnResultSet)
        whenever(mockOldColumnResultSet.next()).thenReturn(oldColumnExists)
        whenever(mockMetaData.getColumns(null, null, table, newColumn)).thenReturn(mockNewColumnResultSet)
        whenever(mockNewColumnResultSet.next()).thenReturn(newColumnExists)
    }

    @Test
    fun `migration renames the column and keeps the data when only the old column exists`() {
        stubSchema(tableExists = true, oldColumnExists = true, newColumnExists = false)

        migration.migrate(mockContext)

        val sql = argumentCaptor<String>()
        verify(mockStatement).execute(sql.capture())
        assertEquals("ALTER TABLE $table RENAME COLUMN $oldColumn TO $newColumn", sql.firstValue)
    }

    @Test
    fun `migration copies the values and drops the old column when both columns exist`() {
        stubSchema(tableExists = true, oldColumnExists = true, newColumnExists = true)

        migration.migrate(mockContext)

        val sql = argumentCaptor<String>()
        verify(mockStatement, org.mockito.kotlin.times(2)).execute(sql.capture())
        assertTrue(sql.firstValue.contains("SET $newColumn = COALESCE($newColumn, $oldColumn)"))
        assertEquals("ALTER TABLE $table DROP COLUMN $oldColumn", sql.secondValue)
    }

    @Test
    fun `migration is skipped when the old column does not exist`() {
        stubSchema(tableExists = true, oldColumnExists = false, newColumnExists = true)

        migration.migrate(mockContext)

        verify(mockConnection, never()).createStatement()
    }

    @Test
    fun `migration is skipped when the table does not exist`() {
        stubSchema(tableExists = false, oldColumnExists = false, newColumnExists = false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).createStatement()
    }
}
