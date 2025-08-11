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
            "CREATE TABLE isin_lei_mapping (" +
                "isin varchar(255) NOT NULL, " +
                "lei varchar(255), " +
                "company_id varchar(255), " +
                "PRIMARY KEY (isin)" +
                "); " +
                "CREATE INDEX idx_isin " +
                "ON isin_lei_mapping " +
                "USING gin (isin gin_trgm_ops);",
        )
    }
}
