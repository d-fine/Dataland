package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Represents a data point entity from the data_point_items table for use in migrations.
 */
data class DataPointTableEntity(
    val dataPointId: String,
    val companyId: String,
    val dataPoint: JSONObject,
    var dataPointType: String,
    val reportingPeriod: String,
    val framework: String = "",
    val currentlyActive: Boolean = true,
) {
    /**
     * Executes an UPDATE query to persist changes to the data point in the database.
     * @param context The Flyway migration context
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

        return listOf(
            dataPointId == other.dataPointId,
            companyId == other.companyId,
            dataPointType == other.dataPointType,
            reportingPeriod == other.reportingPeriod,
            framework == other.framework,
            currentlyActive == other.currentlyActive,
            dataPoint.similar(other.dataPoint),
        ).all { it }
    }

    override fun hashCode(): Int {
        var result = dataPointId.hashCode()
        result = 31 * result + dataPoint.toString().hashCode()
        result = 31 * result + framework.hashCode()
        result = 31 * result + currentlyActive.hashCode()
        return result
    }
}
