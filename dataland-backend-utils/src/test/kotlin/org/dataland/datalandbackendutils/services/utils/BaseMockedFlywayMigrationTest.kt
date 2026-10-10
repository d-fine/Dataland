package org.dataland.datalandbackendutils.services.utils

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Base class for unit tests of Java based flyway migrations that only talk to JDBC (no real database involved).
 * It provides the mocked JDBC objects and helpers to stub and verify the executed statements.
 */
abstract class BaseMockedFlywayMigrationTest {
    protected val mockContext = mock<Context>()
    protected val mockConnection = mock<Connection>()
    protected val mockMetaData = mock<DatabaseMetaData>()
    protected val mockResultSet = mock<ResultSet>()
    protected val mockPreparedStatement = mock<PreparedStatement>()

    @BeforeEach
    fun setupJdbcMocks() {
        reset(mockContext, mockConnection, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }

    /**
     * Stubs the table lookup so that all given tables are reported as existing or as missing.
     */
    protected fun stubTablesExist(
        tableNames: Collection<String>,
        tablesExist: Boolean,
    ) {
        tableNames.forEach { tableName ->
            whenever(mockMetaData.getTables(null, null, tableName, null)).thenReturn(mockResultSet)
        }
        whenever(mockResultSet.next()).thenReturn(tablesExist)
    }

    /**
     * Stubs the prepared statement creation so that every statement reports the given number of affected rows.
     */
    protected fun stubPreparedStatement(affectedRows: Int) {
        whenever(mockConnection.prepareStatement(any<String>())).thenReturn(mockPreparedStatement)
        whenever(mockPreparedStatement.executeUpdate()).thenReturn(affectedRows)
    }

    /**
     * Verifies that exactly one rename statement with the given SQL and parameters was executed and closed.
     */
    protected fun verifyRenameStatementExecuted(
        expectedSql: String,
        sourceType: String,
        targetType: String,
    ) {
        verify(mockConnection).prepareStatement(expectedSql)
        verify(mockPreparedStatement).setString(1, targetType)
        verify(mockPreparedStatement).setString(2, sourceType)
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }

    /**
     * Verifies that exactly one delete statement with the given SQL and parameter was executed and closed.
     */
    protected fun verifyDeleteStatementExecuted(
        expectedSql: String,
        dataPointType: String,
    ) {
        verify(mockConnection).prepareStatement(expectedSql)
        verify(mockPreparedStatement).setString(1, dataPointType)
        verify(mockPreparedStatement).executeUpdate()
        verify(mockPreparedStatement).close()
    }
}
