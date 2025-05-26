package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Function that provides a list of all DataPointTableEntities with respect to a certain data type
 */

fun getDataPointTableEntitiesWithRespectToDataType(
    context: Context?,
    dataPointType: String,
): List<DataPointTableEntity> {
    val objectMapper = ObjectMapper()
    val preparedStatement =
        context!!.connection.prepareStatement(
            "SELECT * from public.data_point_items WHERE data_point_type = ?",
        )
    preparedStatement.setString(1, dataPointType)
    val getQueryResultSet = preparedStatement.executeQuery()

    val listOfDataPointTableEntities = mutableListOf<DataPointTableEntity>()
    while (getQueryResultSet.next()) {
        listOfDataPointTableEntities.add(
            DataPointTableEntity(
                getQueryResultSet.getString("data_point_id"),
                getQueryResultSet.getString("company_id"),
                JSONObject(
                    objectMapper.readValue(
                        getQueryResultSet.getString("data"), String::class.java,
                    ),
                ),
                dataPointType,
                getQueryResultSet.getString("reporting_period"),
            ),
        )
    }

    return listOfDataPointTableEntities
}
