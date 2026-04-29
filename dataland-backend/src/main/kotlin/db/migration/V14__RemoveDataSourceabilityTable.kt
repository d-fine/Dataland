package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script removes the data_sourceability table since it is no longer needed.
 */
@Suppress("ClassName")
class V14__RemoveDataSourceabilityTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DROP TABLE IF EXISTS data_sourceability",
        )
    }
}
