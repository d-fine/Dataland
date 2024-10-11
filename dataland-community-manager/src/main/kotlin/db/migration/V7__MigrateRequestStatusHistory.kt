package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.util.UUID

/**
 * This migration script migrates the request status history to a separate table
 */
@Suppress("ClassName")
class V7__MigrateRequestStatusHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!
        createStatusHistoryTable(context)
        insertStatusHistoryForExistingRequests(context)
        dropRequestStatusColumn(context)
    }

    private fun createStatusHistoryTable(context: Context) {
        context.connection.createStatement().execute(
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

    private fun insertStatusHistoryForExistingRequests(context: Context) {
        val oldRequests =
            context.connection.createStatement().executeQuery(
                "SELECT data_request_id, last_modified_date, request_status FROM data_requests",
            )
        val insertQuery: String =
            "INSERT INTO request_status_history " +
                "(status_history_id, data_request_id, request_status, creation_timestamp) " +
                "VALUES (?, ?, ?, ?)"
        val preparedStatement = context.connection.prepareStatement(insertQuery)

        while (oldRequests.next()) {
            val statusHistoryId = UUID.randomUUID().toString()
            val dataRequestId = oldRequests.getString("data_request_id")
            val requestStatus = oldRequests.getString("request_status")
            val creationTimestamp = oldRequests.getLong("last_modified_date")

            var index = 1
            preparedStatement.setString(index++, statusHistoryId)
            preparedStatement.setString(index++, dataRequestId)
            preparedStatement.setString(index++, requestStatus)
            preparedStatement.setLong(index, creationTimestamp)
            preparedStatement.executeUpdate()
        }
        oldRequests.close()
        preparedStatement.close()
    }

    private fun dropRequestStatusColumn(context: Context) {
        context.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "DROP COLUMN request_status",
        )
    }
}
