package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script creates an index for a faster search for company names and identifiers
 */
@Suppress("ClassName")
class V8__GenerateIsinLeiIndex : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE EXTENSION pg_trgm; " +
                "CREATE INDEX idx_isin " +
                "ON isin_lei_mapping " +
                "USING gin (isin gin_trgm_ops);",
        )
    }
}
