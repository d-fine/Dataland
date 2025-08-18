package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script removes the ISIN-Identifiers from the company identifiers table since they are stored separately now
 */
@Suppress("ClassName")
class V9__RemoveIsins : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DELETE FROM company_identifiers " +
                "WHERE identifier_type='Isin';",
        )
    }
}
