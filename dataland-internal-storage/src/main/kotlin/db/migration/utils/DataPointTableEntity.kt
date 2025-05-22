package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Class that holds the data ID and the company associated data
 */
data class DataPointTableEntity(
    val dataPointId: String,
    val companyID: String,
    val companyAssociatedData: JSONObject,
    var dataPointType: String,
    val reportingPeriod: String,
) {
//    companion object {
//        /**
//         * Constructs a DataPointTableEntity from a dataPointId, dataPointType and dataPoint object
//         */
//        fun pointFromJsonObject(
//            dataPointId: String,
//            companyAssociatedData: JSONObject,
//            dataPointType: String,
//            reportingPeriod: String,
//        ): DataPointTableEntity = DataPointTableEntity(dataPointId, companyAssociatedData, dataPointType, reportingPeriod)
//    }

    /**
     * Method to get a query that writes the company associated data to the corresponding table entry
     */
    fun executeUpdateQuery(context: Context) {
        val queryStatement =
            context.connection.prepareStatement(
                "UPDATE data_point_items SET data = ?, data_point_type=? WHERE data_point_id = ?",
            )
        queryStatement.setString(
            1,
            ObjectMapper().writeValueAsString(companyAssociatedData.toString()),
        )
        queryStatement.setString(2, dataPointType)
        queryStatement.setString(3, dataPointId)
        queryStatement.executeUpdate()
    }

//    val dataPointJsonObject: JSONObject
//        get() = JSONObject(companyAssociatedData)
//
//    val jsonObjectWithoutData: JSONObject
//        get() {
//            val companyAssociatedDataWithoutData = JSONObject(companyAssociatedData)
//            companyAssociatedDataWithoutData.remove()
//            return companyAssociatedDataWithoutData
//        }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as DataPointTableEntity
//
//        if (dataPointId != other.dataPointId) return false
//
//        return dataPointJsonObject.similar(other.dataPointJsonObject) &&
//            jsonObjectWithoutData.similar(other.jsonObjectWithoutData)
//    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataPointTableEntity) return false
        return dataPointId == other.dataPointId &&
            companyID == other.companyID &&
            dataPointType == other.dataPointType &&
            reportingPeriod == other.reportingPeriod &&
            companyAssociatedData.similar(other.companyAssociatedData)
    }

    override fun hashCode(): Int {
        var result = dataPointId.hashCode()
        result = 31 * result + companyAssociatedData.toString().hashCode()
        return result
    }
}
