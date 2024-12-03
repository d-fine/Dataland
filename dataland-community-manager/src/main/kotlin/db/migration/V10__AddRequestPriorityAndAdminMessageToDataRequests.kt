package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates closed data_request to resolved data_requests
 */
@Suppress("ClassName")
class V10__AddRequestPriorityAndAdminMessageToDataRequests : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "ADD COLUMN request_priority varchar(255) NOT NULL DEFAULT 'Normal', " +
                "ADD COLUMN adminComment text",
        )
    }
}
