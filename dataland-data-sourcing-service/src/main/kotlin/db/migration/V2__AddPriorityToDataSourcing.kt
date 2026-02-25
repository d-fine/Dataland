package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Adds the priority column to the data_sourcing table.
 */
@Suppress("ClassName")
class V2__AddPriorityToDataSourcing : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val hasTable = connection.metaData.getTables(null, null, "data_sourcing", null).next()

        if (!hasTable) return

        connection.createStatement().execute(
            "ALTER TABLE data_sourcing ADD COLUMN IF NOT EXISTS priority INTEGER NOT NULL DEFAULT 10;",
        )
    }
}
