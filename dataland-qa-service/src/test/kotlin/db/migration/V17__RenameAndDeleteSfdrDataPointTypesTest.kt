package db.migration

import org.flywaydb.core.api.migration.Context
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
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("ClassName")
class V17__RenameAndDeleteSfdrDataPointTypesTest {
    private val migration = V17__RenameAndDeleteSfdrDataPointTypes()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockResultSet = mock<ResultSet>()
    private val mockPreparedStatement = mock<PreparedStatement>()

    private val sourceType = V17__RenameAndDeleteSfdrDataPointTypes.renameMap.keys.first()
    private val targetType = V17__RenameAndDeleteSfdrDataPointTypes.renameMap.values.first()
    private val deletedType = V17__RenameAndDeleteSfdrDataPointTypes.deletedDataPointTypes.first()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        V17__RenameAndDeleteSfdrDataPointTypes.tablesWithDataPointType.forEach { tableName ->
            whenever(mockMetaData.getTables(null, null, tableName, null)).thenReturn(mockResultSet)
        }
        whenever(mockResultSet.next()).thenReturn(false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that data point type is renamed in selected QA table`() {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(2)

        migration.renameDataPointType(
            context = mockContext,
            tableName = "data_point_qa_review",
            sourceType = sourceType,
            targetType = targetType,
        )

        verify(mockConnection).prepareStatement(
            "UPDATE data_point_qa_review SET data_point_type = ? WHERE data_point_type = ?",
        )
        verify(mockPreparedStatement).setString(1, targetType)
        verify(mockPreparedStatement).setString(2, sourceType)
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }

    @Test
    fun `check that data point type is deleted in selected QA table`() {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(1)

        migration.deleteDataPointType(
            context = mockContext,
            tableName = "data_point_qa_review",
            dataPointType = deletedType,
        )

        verify(mockConnection).prepareStatement(
            "DELETE FROM data_point_qa_review WHERE data_point_type = ?",
        )
        verify(mockPreparedStatement).setString(1, deletedType)
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }

    @Test
    fun `check that migration renames and deletes in all tables when present`() {
        V17__RenameAndDeleteSfdrDataPointTypes.tablesWithDataPointType.forEach { tableName ->
            whenever(mockMetaData.getTables(null, null, tableName, null)).thenReturn(mockResultSet)
        }
        whenever(mockResultSet.next()).thenReturn(true)
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(1)

        migration.migrate(mockContext)

        val statementsPerTable =
            V17__RenameAndDeleteSfdrDataPointTypes.renameMap.size +
                V17__RenameAndDeleteSfdrDataPointTypes.deletedDataPointTypes.size
        val totalStatements = statementsPerTable * V17__RenameAndDeleteSfdrDataPointTypes.tablesWithDataPointType.size
        verify(mockConnection, times(totalStatements)).prepareStatement(any<String>())
    }
}
