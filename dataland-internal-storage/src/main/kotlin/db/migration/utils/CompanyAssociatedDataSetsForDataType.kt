package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Method to get the company associated dataset for a given data type
 */
fun getCompanyAssociatedDatasetsForDataType(context: Context?, dataType: String): List<DataTableEntity> {
    val objectMapper = ObjectMapper()
    val getQueryResultSet = context!!.connection.createStatement().executeQuery(
        "SELECT * from data_items " +
            "WHERE data LIKE '%\\\\\\\"dataType\\\\\\\":\\\\\\\"${dataType}\\\\\\\"%'",
    )
    val companyAssociatedDatasets = mutableListOf<DataTableEntity>()
    while (getQueryResultSet.next()) {
        companyAssociatedDatasets.add(
            DataTableEntity(
                getQueryResultSet.getString("data_id"),
                JSONObject(
                    objectMapper.readValue(
                        getQueryResultSet.getString("data"), String::class.java,
                    ),
                ),
            ),
        )
    }

    return companyAssociatedDatasets.filter {
            dataTableEntity ->
        dataTableEntity.companyAssociatedData.getString("dataType") == dataType
    }
}
