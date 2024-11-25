package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script adds the requestStatusChangeReason column to the request status history table
 */
@Suppress("ClassName")
class V10_AddRequestChangeReasonToHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!
        addRequestStatusChangeReasonColumn(context)
    }

    private fun addRequestStatusChangeReasonColumn(context: Context) {
        context.connection.createStatement().execute(
            "ALTER TABLE request_status_history " +
                "ADD COLUMN request_status_change_reason VARCHAR(255) " +
                "DEFAULT NULL",
        )
    }
}
