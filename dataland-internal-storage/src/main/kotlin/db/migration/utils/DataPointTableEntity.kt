package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Class that holds the data ID and the company associated data
 */
data class DataPointTableEntity(
    val dataPointId: String,
    val companyId: String,
    val dataPoint: JSONObject,
    var dataPointType: String,
    val reportingPeriod: String,
) {
    /**
     * Method to get a query that writes the company associated data and data point type to the corresponding table entry
     */
    fun executeUpdateQuery(context: Context) {
        val queryStatement =
            context.connection.prepareStatement(
                "UPDATE data_point_items SET data = ?, data_point_type=? WHERE data_point_id = ?",
            )
        queryStatement.setString(
            1,
            ObjectMapper().writeValueAsString(dataPoint.toString()),
        )

        queryStatement.setString(2, dataPointType)
        @Suppress("MagicNumber")
        queryStatement.setString(3, dataPointId)
        queryStatement.executeUpdate()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataPointTableEntity) return false
        return dataPointId == other.dataPointId &&
            companyId == other.companyId &&
            dataPointType == other.dataPointType &&
            reportingPeriod == other.reportingPeriod &&
            dataPoint.similar(other.dataPoint)
    }

    override fun hashCode(): Int {
        var result = dataPointId.hashCode()
        result = 31 * result + dataPoint.toString().hashCode()
        return result
    }
}
