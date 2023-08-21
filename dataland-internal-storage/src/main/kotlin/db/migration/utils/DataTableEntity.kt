package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
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
    fun getWriteQuery(): String = "UPDATE data_items " +
        "SET data = '${ObjectMapper().writeValueAsString(companyAssociatedData.toString())}' " +
        "WHERE data_id = '$dataId'"
}
