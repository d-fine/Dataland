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
class V15__RenameGarAssetsDataPointTypesTest {
    private val migration = V15__RenameGarAssetsDataPointTypes()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockResultSet = mock<ResultSet>()
    private val mockPreparedStatement = mock<PreparedStatement>()

    private val sourceType = V15__RenameGarAssetsDataPointTypes.renameMap.keys.first()
    private val targetType = V15__RenameGarAssetsDataPointTypes.renameMap.values.first()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        whenever(mockMetaData.getTables(null, null, "data_point_meta_information", null)).thenReturn(mockResultSet)
        whenever(mockMetaData.getTables(null, null, "data_point_uuid_map", null)).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that data point type is renamed in selected table and column`() {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(3)

        migration.renameDataPointType(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
            sourceType = sourceType,
            targetType = targetType,
        )

        verify(mockConnection).prepareStatement(
            "UPDATE data_point_meta_information SET data_point_type = ? WHERE data_point_type = ?",
        )
        verify(mockPreparedStatement).setString(1, targetType)
        verify(mockPreparedStatement).setString(2, sourceType)
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }
}
