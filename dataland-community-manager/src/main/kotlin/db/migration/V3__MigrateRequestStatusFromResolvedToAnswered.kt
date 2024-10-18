package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script extends the data_requests table
 */
@Suppress("ClassName")
class V3__MigrateRequestStatusFromResolvedToAnswered : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "UPDATE data_requests " +
                "SET request_status = 'Answered' " +
                "WHERE request_status = 'Resolved'",
        )
    }
}
