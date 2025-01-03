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
                "WHEN request_priority = 'Normal' THEN 'Low' " +
                "WHEN request_priority = 'Very High' THEN 'High' " +
                "ELSE request_priority END " +
                "WHERE data_request_id IN (" +
                "SELECT H1.data_request_id " +
                "FROM data_requests AS R " +
                "INNER JOIN request_status_history AS H1 " +
                "ON R.data_request_id = H1.data_request_id " +
                "LEFT JOIN request_status_history AS H2 " +
                "ON (R.data_request_id = H2.data_request_id) " +
                "AND (H1.creation_timestamp < H2.creation_timestamp) " +
                "WHERE (H2.data_request_id IS NULL) " +
                "AND (H1.request_status = 'Open')" +
                ")",
        )
    }
}
