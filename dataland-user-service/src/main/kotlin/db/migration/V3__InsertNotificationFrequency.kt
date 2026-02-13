package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Insert and fill new column NotificationFrequency
 */
@Suppress("ClassName")
class V3__InsertNotificationFrequency : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE IF EXISTS portfolios " +
                "ADD COLUMN IF NOT EXISTS notification_frequency VARCHAR(255) " +
                "NOT NULL DEFAULT 'Weekly'",
        )
    }
}
