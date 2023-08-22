package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Class that holds the data ID and the company associated data
 */
data class DataTableEntity(
    val dataId: String,
    val companyAssociatedData: JSONObject,
) {

    /**
     * Method to get a query that writes the company associated data to the corresponding table entry
     */
    fun executeUpdateQuery(context: Context) {
        val queryStatement = context.connection.prepareStatement(
            "UPDATE data_items " +
                "SET data = ? " +
                "WHERE data_id = '$dataId'",
        )
        queryStatement.setString(1, ObjectMapper().writeValueAsString(companyAssociatedData.toString()))
        queryStatement.executeUpdate()
        println(queryStatement.toString())
    }
}
