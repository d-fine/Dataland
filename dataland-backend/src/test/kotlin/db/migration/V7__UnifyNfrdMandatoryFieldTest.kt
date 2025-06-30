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
class V7__UnifyNfrdMandatoryFieldTest {
    private val migration = V7__UnifyNfrdMandatoryField()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockStatement = mock<PreparedStatement>()
    private val mockResultSet = mock<ResultSet>()

    @BeforeEach
    fun basicSetup() {
        reset(mockContext, mockConnection, mockResultSet, mockStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mock<java.sql.DatabaseMetaData>())
        whenever(mockConnection.metaData.getTables(null, null, "data_point_meta_information", null)).thenReturn(mockResultSet)
        whenever(mockConnection.metaData.getTables(null, null, "data_point_uuid_map", null)).thenReturn(mockResultSet)
    }

    @Test
    fun `check that no migration starts if table does not exist`() {
        whenever(mockResultSet.next()).thenReturn(false)
        migration.migrate(mockContext)
        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `check that migrate calls update for both tables if table exists`() {
        whenever(mockResultSet.next()).thenReturn(true)
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate(any<String>())).thenReturn(2)

        migration.migrate(mockContext)
        verify(mockConnection, times(2)).createStatement()
        verify(mockStatement, times(2)).executeUpdate(
            argThat { input -> input.startsWith("UPDATE") },
        )
        verify(mockStatement, times(2)).close()
    }

    @Test
    fun `check that extendedEnumYesNoNfrdMandatory is updated correctly in data point meta information table`() {
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate(any<String>())).thenReturn(2)

        migration.migrateNfrdMandatoryField(
            context = mockContext,
            tableName = "data_point_meta_information",
            columnName = "data_point_type",
        )

        verify(mockStatement).executeUpdate(
            "UPDATE data_point_meta_information SET data_point_type = 'extendedEnumYesNoIsNfrdMandatory' " +
                "WHERE data_point_type = 'extendedEnumYesNoNfrdMandatory'",
        )
        verify(mockStatement).close()
    }

    @Test
    fun `check that extendedEnumYesNoNfrdMandatory is updated correctly in data point uuid map`() {
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate(any<String>())).thenReturn(2)

        migration.migrateNfrdMandatoryField(
            context = mockContext,
            tableName = "data_point_uuid_map",
            columnName = "data_point_identifier",
        )

        verify(mockStatement).executeUpdate(
            "UPDATE data_point_uuid_map SET data_point_identifier = 'extendedEnumYesNoIsNfrdMandatory' " +
                "WHERE data_point_identifier = 'extendedEnumYesNoNfrdMandatory'",
        )
        verify(mockStatement).close()
    }
}
