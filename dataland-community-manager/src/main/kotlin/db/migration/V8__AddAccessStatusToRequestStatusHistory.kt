package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates the request status history to a separate table
 */
class V8__AddAccessStatusToRequestStatusHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!
        addAccessStatusToStatusHistoryTable(context)
        migrateSmeToVsme(context)
    }

    // TODO add new column accessStatus and migrate datatype sme -> vsme
    private fun addAccessStatusToStatusHistoryTable(context: Context) {
        context.connection.createStatement().execute(
            "ALTER TABLE request_status_history" +
                "ADD COLUMN accessStatus varchar(255) NOT NULL DEFAULT 'Public',",
        )
    }
    private fun migrateSmeToVsme(context: Context) {
        context.connection.createStatement().execute(
            "UPDATE data_requests " +
                "SET data_type = 'sme' " +
                "WHERE data_type = 'vsme'",
        )
    }
}
