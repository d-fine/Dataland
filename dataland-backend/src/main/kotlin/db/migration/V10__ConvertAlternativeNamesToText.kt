package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script converts the column type of 'company_alternative_name' in the
 * 'stored_company_entity_alternative_names' table from VARCHAR(255) to TEXT.
 * This change allows for storing longer alternative names without truncation.
 */
@Suppress("ClassName")
class V10__ConvertAlternativeNamesToText : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!
            .connection
            .createStatement()
            .execute("ALTER TABLE stored_company_entity_company_alternative_names ALTER COLUMN company_alternative_names TYPE text;")
    }
}
