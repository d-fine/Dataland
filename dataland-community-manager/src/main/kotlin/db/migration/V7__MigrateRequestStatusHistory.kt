package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates the request status history to a separate table
 */
class V7__MigrateRequestStatusHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE request_status_history (" +
                "status_history_id varchar(255) NOT NULL, " +
                "data_request_id varchar(255) NOT NULL, " +
                "request_status varchar(255) NOT NULL, " +
                "creation_timestamp bigint NOT NULL, " +
                "PRIMARY KEY (status_history_id), " +
                "CONSTRAINT fk_data_request FOREIGN KEY(data_request_id) REFERENCES data_requests(data_request_id)" +
                ")",
        )
    }
}
