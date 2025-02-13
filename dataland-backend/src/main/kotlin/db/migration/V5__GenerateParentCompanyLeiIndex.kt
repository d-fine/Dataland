package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script creates an index for a faster search for parent company leis in stored_companies
 */
@Suppress("ClassName")
class V5__GenerateParentCompanyLeiIndex : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE INDEX idx_parent_company_lei " +
                "ON stored_companies (parent_company_lei);",
        )
    }
}
