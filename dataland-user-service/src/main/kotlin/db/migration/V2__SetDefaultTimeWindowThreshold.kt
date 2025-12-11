package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script sets the default timeWindowThreshold to 'Standard'
 * for existing portfolios that were created before this field was added.
 */
@Suppress("ClassName")
class V2__SetDefaultTimeWindowThreshold : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "UPDATE portfolios " +
                "SET time_window_threshold = 'Standard' " +
                "WHERE time_window_threshold IS NULL",
        )
    }
}
