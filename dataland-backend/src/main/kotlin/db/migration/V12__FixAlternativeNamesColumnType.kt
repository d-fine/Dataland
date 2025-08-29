package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script resets the type of column company_alternative_names.
 */
@Suppress("ClassName")
class V12__FixAlternativeNamesColumnType : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val stmt = context!!.connection.createStatement()

        stmt.execute(
            """
            ALTER TABLE stored_company_entity_company_alternative_names
            ALTER COLUMN company_alternative_names TYPE TEXT USING company_alternative_names::TEXT;
            """.trimIndent(),
        )
    }
}
