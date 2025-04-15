package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.ResultSet

/**
 * This migration script adds an index for the company ID search
 */
@Suppress("ClassName")
class V3__AddIndexToSpeedUpSearches : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        if (tableExists(context!!, "document_meta_info_company_ids")) {
            createIndexForCompanyIds(context!!)
        }
    }

    private fun tableExists(context: Context, tableName: String): Boolean {
        val connection = context.connection
        val resultSet: ResultSet = connection.metaData.getTables(null, null, tableName, null)
        return resultSet.next()
    }

    private fun createIndexForCompanyIds(context: Context) {
        context.connection.createStatement().execute(
            """
            CREATE INDEX IF NOT EXISTS idx_company_ids ON document_meta_info_company_ids (company_id)
            """.trimIndent(),
        )
    }
}
