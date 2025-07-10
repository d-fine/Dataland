package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script removes an unused column the actually used column is called admin_comment
 */
@Suppress("ClassName")
class V15__DropUnusedColumn : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "DROP COLUMN admincomment",
        )
    }
}
