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
import java.sql.Statement

@Suppress("ClassName")
class V10__UnifyNfrdMandatoryFieldTest {
    private val migration = V10__UnifyNfrdMandatoryField()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockStatement = mock<Statement>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockResultSet = mock<ResultSet>()
    private val mockPreparedStatement = mock<PreparedStatement>()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockStatement, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }

    @Test
    fun `check that migration does not start if tables are missing`() {
        whenever(
            mockMetaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `sample check that migrateNfrdMandatoryField updates extendedEnumYesNoNfrdMandatory correctly`() {
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate(any<String>())).thenReturn(2)

        migration.migrateNfrdMandatoryField(mockContext, "data_point_qa_review")

        verify(mockStatement).executeUpdate(
            "UPDATE data_point_qa_review SET data_point_type = 'extendedEnumYesNoIsNfrdMandatory' " +
                "WHERE data_point_type = 'extendedEnumYesNoNfrdMandatory'",
        )
        verify(mockStatement).close()
    }
}
