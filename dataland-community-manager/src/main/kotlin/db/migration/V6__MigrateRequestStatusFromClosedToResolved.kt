package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates closed data_request to resolved data_requests
 */
@Suppress("ClassName")
class V6__MigrateRequestStatusFromClosedToResolved : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "UPDATE data_requests " +
                "SET request_status = 'Resolved' " +
                "WHERE request_status = 'Closed'",
        )
    }
}
