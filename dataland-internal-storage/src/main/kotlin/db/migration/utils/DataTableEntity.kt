package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject

data class DataTableEntity(
    val dataId: String,
    val companyAssociatedData: JSONObject,
) {
    fun getWriteQuery(): String = "UPDATE data_items " +
        "SET data = '${ObjectMapper().writeValueAsString(companyAssociatedData.toString())}' " +
        "WHERE data_id = '$dataId'"
}
