package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Removes the database tables that backed the legacy "data request" feature, which has been replaced by
 * the request functionality provided by the data-sourcing service. Also purges any notification events
 * whose type is no longer supported.
 */
@Suppress("ClassName")
class V18__RemoveLegacyRequestTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val notificationEventsTable = "notification_events"
        val notificationEventsTableExists =
            context!!
                .connection.metaData
                .getTables(null, null, notificationEventsTable, null)
                .next()

        if (notificationEventsTableExists) {
            context.connection.createStatement().execute(
                "DELETE FROM notification_events WHERE notification_event_type <> 'InvestorRelationsEvent'",
            )
        }

        context.connection.createStatement().execute("DROP TABLE IF EXISTS messages")
        context.connection.createStatement().execute("DROP TABLE IF EXISTS request_status_history")
        context.connection.createStatement().execute("DROP TABLE IF EXISTS data_requests")
    }
}
