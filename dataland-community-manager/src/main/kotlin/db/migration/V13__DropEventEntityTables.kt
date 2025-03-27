package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * The two tables dropped below contain information about unsent company notifications.
 * In the future we will use a different messaging logic, so that we don't need them anymore.
 */
@Suppress("ClassName")
class V13__DropEventEntityTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute("DROP TABLE IF EXISTS elementary_events;")
        context.connection.createStatement().execute("DROP TABLE IF EXISTS notification_events;")
    }
}
