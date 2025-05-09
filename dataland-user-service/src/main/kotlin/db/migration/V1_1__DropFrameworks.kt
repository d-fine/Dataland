package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial database table
 */
@Suppress("ClassName")
class V1_1__DropFrameworks : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DROP TABLE IF EXISTS data_types",
        )
    }
}
