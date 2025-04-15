package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script adds an index for the company ID search
 */
@Suppress("ClassName")
class V3__AddIndexToSpeedUpSearches : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        createIndexForCompanyIds(context!!)
    }

    private fun createIndexForCompanyIds(context: Context) {
        context.connection.createStatement().execute(
            """
            ALTER TABLE IF EXISTS document_meta_info_company_ids
            CREATE INDEX idx_company_ids ON document_meta_info_company_ids (company_id);
            """.trimIndent(),
        )
    }
}
