package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script creates an index for a faster search for company names and identifiers
 */
@Suppress("ClassName")
class V2__GenerateCompanyNameIdentifierIndex : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE EXTENSION pg_trgm; " +
                "CREATE INDEX idx_company_identifier " +
                "ON company_identifiers " +
                "USING gin (identifier_value gin_trgm_ops); " +
                "CREATE INDEX idx_company_name " +
                "ON stored_companies " +
                "USING gin (company_name gin_trgm_ops); " +
                "CREATE INDEX idx_company_alt_name " +
                "ON stored_company_entity_company_alternative_names " +
                "USING gin (company_alternative_names gin_trgm_ops); " +
                "CREATE INDEX idx_company_id_alt_name " +
                "ON stored_company_entity_company_alternative_names(stored_company_entity_company_id); " +
                "CREATE INDEX idx_company_id_identifiers " +
                "ON company_identifiers(company_id); " +
                "CREATE INDEX idx_company_id " +
                "ON stored_companies(company_id);",
        )
    }
}
