package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.util.UUID

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
        val oldRequests = context.connection.createStatement().executeQuery(
            "SELECT data_request_id, last_modified_date, request_status FROM data_requests",
        )
        val insertQuery: String = "INSERT INTO request_status_history " +
            "(status_history_id, data_request_id, request_status, creation_timestamp) " +
            "VALUES (?, ?, ?, ?)"
        val preparedStatement = context.connection.prepareStatement(insertQuery)

        while (oldRequests.next()) {
            val status_history_id = UUID.randomUUID().toString()
            val data_request_id = oldRequests.getString("data_request_id")
            val request_status = oldRequests.getString("request_status")
            val creation_time_stamp = oldRequests.getLong("last_modified_date")

            preparedStatement.setString(1, status_history_id)
            preparedStatement.setString(2, data_request_id)
            preparedStatement.setString(3, request_status)
            preparedStatement.setLong(4, creation_time_stamp)
            preparedStatement.executeUpdate()
        }
        oldRequests.close()
        preparedStatement.close()

        context.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "DROP COLUMN request_status",
        )
    }
}
