package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates the request status history to a separate table
 */
class V8__AddAccessStatusToRequestStatusHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!
        migrateSmeToVsme(context)
        addAccessStatusToStatusHistoryTable(context)
        setInitialAccessStatusDependingOnDataType(context)
        makeAccessStatusRowNonNullable(context)
    }

    private fun addAccessStatusToStatusHistoryTable(context: Context) {
        context.connection.createStatement().execute(
            "ALTER TABLE request_status_history " +
                "ADD COLUMN access_status varchar(255) ",
        )
    }
    private fun makeAccessStatusRowNonNullable(context: Context) {
        context.connection.createStatement().execute(
            "ALTER TABLE request_status_history " +
                "ALTER COLUMN access_status SET NOT NULL ",
        )
    }
    private fun migrateSmeToVsme(context: Context) {
        context.connection.createStatement().execute(
            "UPDATE data_requests " +
                "SET data_type = 'vsme' " +
                "WHERE data_type = 'sme' ",
        )
    }
    private fun setInitialAccessStatusDependingOnDataType(context: Context) {
        val requestsForNonVsmeDataType = context.connection.createStatement().executeQuery(
            "SELECT data_request_id FROM data_requests WHERE data_type != 'vsme' ",
        )
        val requestsForVsmeDataType = context.connection.createStatement().executeQuery(
            "SELECT data_request_id FROM data_requests WHERE data_type = 'vsme' ",
        )

        val query = "UPDATE request_status_history SET access_status = ? WHERE data_request_id = ?"

        while (requestsForNonVsmeDataType.next()) {
            val requestId = requestsForNonVsmeDataType.getString("data_request_id")

            val preparedStatement = context.connection.prepareStatement(query)
            preparedStatement.setString(1, "Public")
            preparedStatement.setString(2, requestId)

            preparedStatement.executeUpdate()
        }
        requestsForNonVsmeDataType.close()

        while (requestsForVsmeDataType.next()) {
            val requestId = requestsForVsmeDataType.getString("data_request_id")
            val preparedStatement = context.connection.prepareStatement(query)
            preparedStatement.setString(1, "Declined")
            preparedStatement.setString(2, requestId)

            preparedStatement.executeUpdate()
        }
        requestsForVsmeDataType.close()
    }
}
