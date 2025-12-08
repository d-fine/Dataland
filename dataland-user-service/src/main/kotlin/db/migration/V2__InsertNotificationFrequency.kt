package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Insert and fill new column NotificationFrequency
 */
@Suppress("ClassName")
class V2__InsertNotificationFrequency : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE portfolios " +
                "ADD COLUMN IF NOT EXISTS notificationFrequency VARCHAR(255) " +
                "NOT NULL DEFAULT 'weekly';",
        )
    }
}
