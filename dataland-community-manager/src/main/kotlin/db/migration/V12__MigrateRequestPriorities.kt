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
            """
            WITH LatestStatus AS (
                SELECT 
                    H1.*, 
                    ROW_NUMBER() OVER (PARTITION BY H1.data_request_id ORDER BY H1.creation_timestamp DESC) AS row_num
                FROM request_status_history AS H1
            )
            UPDATE data_requests
            SET request_priority = CASE 
                WHEN request_priority = 'Normal' THEN 'Low'
                WHEN request_priority = 'Very High' THEN 'High'
                ELSE request_priority 
            END
            WHERE data_request_id IN (
                SELECT data_request_id
                FROM LatestStatus
                WHERE row_num = 1
                AND request_status = 'Open'
            )
            """,
        )
    }
}
