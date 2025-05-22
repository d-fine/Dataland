package db.migration.utils

import org.flywaydb.core.api.migration.Context

/**
 * Class that holds the data point ID and the data point type
 */
data class DataPointIdAndDataPointTypeEntity(
    val dataPointId: String,
    var dataPointType: String,
) {
    /**
     * Method to get a query that writes the data point type to the corresponding table entry
     */
    fun executeUpdateQuery(context: Context) {
        val queryStatement =
            context.connection.prepareStatement(
                "UPDATE data_point_items SET data_point_type= ? WHERE data_point_id = ?",
            )
        queryStatement.setString(1, dataPointType)
        queryStatement.setString(2, dataPointId)
        queryStatement.executeUpdate()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataPointIdAndDataPointTypeEntity) return false
        return dataPointId == other.dataPointId &&
            dataPointType == other.dataPointType
    }

    override fun hashCode(): Int {
        var result = dataPointId.hashCode()
        result = 31 * result + dataPointType.hashCode()
        return result
    }
}
