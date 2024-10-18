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
    companion object {
        /**
         * Constructs a DataTableEntity from a dataId, dataType and data object
         */
        fun fromJsonObject(
            dataId: String,
            dataType: String,
            data: JSONObject,
        ): DataTableEntity {
            val companyAssociatedData = JSONObject()
            companyAssociatedData.put("dataType", dataType)
            companyAssociatedData.put("data", data.toString())
            return DataTableEntity(dataId, companyAssociatedData)
        }
    }

    /**
     * Method to get a query that writes the company associated data to the corresponding table entry
     */
    fun executeUpdateQuery(context: Context) {
        val queryStatement =
            context.connection.prepareStatement(
                "UPDATE data_items " +
                    "SET data = ? " +
                    "WHERE data_id = ?",
            )
        queryStatement.setString(1, ObjectMapper().writeValueAsString(companyAssociatedData.toString()))
        queryStatement.setString(2, dataId)
        queryStatement.executeUpdate()
    }

    val dataJsonObject: JSONObject
        get() = JSONObject(companyAssociatedData.getString("data"))

    val jsonObjectWithoutData: JSONObject
        get() {
            val companyAssociatedDataWithoutData = JSONObject(companyAssociatedData.toString())
            companyAssociatedDataWithoutData.remove("data")
            return companyAssociatedDataWithoutData
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataTableEntity

        if (dataId != other.dataId) return false

        return dataJsonObject.similar(other.dataJsonObject) &&
            jsonObjectWithoutData.similar(other.jsonObjectWithoutData)
    }

    override fun hashCode(): Int {
        var result = dataId.hashCode()
        result = 31 * result + companyAssociatedData.toString().hashCode()
        return result
    }
}
