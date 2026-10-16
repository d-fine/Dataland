package db.migration

import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.Statement

@Suppress("ClassName")
class V17__ReworkCompanyProxyRelationsTest {
    private val migration = V17__ReworkCompanyProxyRelations()
    private val mockContext = mock<Context>()
    private val mockConnection = mock<Connection>()
    private val mockMetaData = mock<DatabaseMetaData>()
    private val mockResultSet = mock<ResultSet>()
    private val mockStatement = mock<Statement>()

    private fun setup(tableExists: Boolean) {
        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(mockMetaData.getTables(null, null, "company_proxy_relations", null)).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(tableExists)
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
    }

    @Test
    fun `check that migration does nothing if the table does not exist`() {
        setup(false)
        migration.migrate(mockContext)
        verify(mockConnection, never()).createStatement()
    }

    @Test
    fun `check that all statements are executed if the table exists`() {
        setup(true)
        migration.migrate(mockContext)
        verify(mockStatement, times(V17__ReworkCompanyProxyRelations.statements.size)).execute(any<String>())
    }
}
