package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates open requests to the following priorities:
 * Normal -> Low
 * Very High -> High
 * all other request priorites remain unchanged
 */
@Suppress("ClassName")
class V12__MigrateRequestPriorities : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "UPDATE data_requests " +
                "SET request_priority = CASE " +
                "WHEN request_priority = 'Normal' AND request_status = 'Open' THEN 'Low' " +
                "WHEN request_priority = 'Very High' AND request_status = 'Open' THEN 'High' " +
                "ELSE request_priority END",
        )
    }
}
