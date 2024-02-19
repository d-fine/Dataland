package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates the message history to a separate table
 */
class V3__MigrateMessageHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        // The message_history is simply dropped because there is no data on for this, yet.
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "RENAME COLUMN data_type_name to data_type",
        )
        context.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "DROP COLUMN message_history",
        )
        context.connection.createStatement().execute(
            "CREATE TABLE messages (" +
                "message_id varchar(255) NOT NULL, " +
                "data_request_id varchar(255) NOT NULL, " +
                "contacts text, " +
                "message text, " +
                "last_modified_date bigint NOT NULL, " +
                "PRIMARY KEY (message_id), " +
                "CONSTRAINT fk_data_request FOREIGN KEY(data_request_id) REFERENCES data_requests(data_request_id)" +
                ")",
        )
    }
}
