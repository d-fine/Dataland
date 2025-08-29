package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script converts the column type of 'company_alternative_name' in the
 * 'stored_company_entity_alternative_names' table from VARCHAR(255) to TEXT.
 * This change allows for storing longer alternative names without truncation.
 * This is an updated version of the script, deleting the original column and setting up a new one.
 */
@Suppress("ClassName")
class V11__ConvertAlternativeNamesToTextUpdated : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val stmt = context!!.connection.createStatement()

        stmt.execute(
            """
            ALTER TABLE stored_company_entity_company_alternative_names
            ADD COLUMN company_alternative_names_text TEXT;
            """.trimIndent(),
        )

        stmt.execute(
            """
            UPDATE stored_company_entity_company_alternative_names
            SET company_alternative_names_text = company_alternative_names;
            """.trimIndent(),
        )

        stmt.execute(
            """
            ALTER TABLE stored_company_entity_company_alternative_names 
            DROP COLUMN company_alternative_names;
            """.trimIndent(),
        )

        stmt.execute(
            """
            ALTER TABLE stored_company_entity_company_alternative_names 
            RENAME COLUMN company_alternative_names_text TO company_alternative_names;
            """.trimIndent(),
        )
    }
}
