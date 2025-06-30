package db.migration.utils

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

abstract class BaseDatabaseMigrationTest {
    protected val mockContext = mock<Context>()
    protected val mockConnection = mock<Connection>()
    protected val mockStatement = mock<Statement>()
    protected val mockMetaData = mock<DatabaseMetaData>()
    protected val mockResultSet = mock<ResultSet>()
    protected val mockPreparedStatement = mock<PreparedStatement>()

    @BeforeEach
    fun setup() {
        reset(mockContext, mockConnection, mockStatement, mockMetaData, mockResultSet, mockPreparedStatement)
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
    }
}
