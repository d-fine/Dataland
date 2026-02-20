package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Adds a non-nullable priority column to the data_sourcing table, defaulting to 10.
 */
@Suppress("ClassName")
class V2__AddPriorityToDataSourcing : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_sourcing ADD COLUMN priority INTEGER NOT NULL DEFAULT 10;",
        )
    }
}
