package db.migration

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

@Suppress("ClassName")
class V15__SeedInitialQaConfigTest {
    private val migration = V15__SeedInitialQaConfig()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockStatement = mock<Statement>()
    private val mockPreparedStatement = mock<PreparedStatement>()
    private val mockTablesResultSet = mock<ResultSet>()
    private val mockCountResultSet = mock<ResultSet>()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockMetaData, mockStatement, mockPreparedStatement, mockTablesResultSet, mockCountResultSet)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
    }

    private fun stubTables(tablesExist: Boolean) {
        whenever(mockMetaData.getTables(isNull(), isNull(), any<String>(), isNull())).thenReturn(mockTablesResultSet)
        whenever(mockTablesResultSet.next()).thenReturn(tablesExist)
    }

    private fun stubRowCount(count: Int) {
        whenever(mockStatement.executeQuery(any<String>())).thenReturn(mockCountResultSet)
        whenever(mockCountResultSet.next()).thenReturn(true)
        whenever(mockCountResultSet.getInt(1)).thenReturn(count)
    }

    @Test
    fun `migration creates tables and inserts exactly one row when nothing exists yet`() {
        stubTables(tablesExist = false)
        stubRowCount(count = 0)

        migration.migrate(mockContext)

        verify(mockPreparedStatement).setObject(1, QaConfigEntity.QA_CONFIG_SINGLETON_ID)
        verify(mockPreparedStatement).setString(
            2,
            JsonUtils.defaultObjectMapper.writeValueAsString(V15__SeedInitialQaConfig.initialConfig),
        )
        verify(mockPreparedStatement).executeUpdate()
    }

    @Test
    fun `migration is skipped (no insert) when a row already exists`() {
        stubTables(tablesExist = true)
        stubRowCount(count = 1)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `seeded config matches expected defaults`() {
        val config = V15__SeedInitialQaConfig.initialConfig

        assertEquals(0.0, config.samplingProbability)
        assertEquals(0.5, config.decimalRelativeThreshold)
        assertEquals(5L, config.integerAbsoluteThreshold)
        assertTrue(config.individualDecimalThresholds.isEmpty())
        assertTrue(config.individualIntegerThresholds.isEmpty())
        assertTrue(config.autoPreApprovalEnabled)
        assertFalse(config.exemptFields[DataTypeEnum.sfdr].isNullOrEmpty())
        assertNull(config.submitUserId)
    }
}
