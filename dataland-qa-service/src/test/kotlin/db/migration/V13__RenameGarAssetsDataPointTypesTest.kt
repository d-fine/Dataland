package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("ClassName")
class V13__RenameGarAssetsDataPointTypesTest {
    private val migration = V13__RenameGarAssetsDataPointTypes()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockResultSet = mock<ResultSet>()
    private val mockPreparedStatement = mock<PreparedStatement>()

    private val sourceType = V13__RenameGarAssetsDataPointTypes.renameMap.keys.first()
    private val targetType = V13__RenameGarAssetsDataPointTypes.renameMap.values.first()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        V13__RenameGarAssetsDataPointTypes.tablesWithDataPointType.forEach { tableName ->
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
}
