package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Removes the legacy access status column from request status history.
 */
@Suppress("ClassName")
class V17__DropAccessStatusFromRequestStatusHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE request_status_history " +
                "DROP COLUMN access_status",
        )
    }
}
