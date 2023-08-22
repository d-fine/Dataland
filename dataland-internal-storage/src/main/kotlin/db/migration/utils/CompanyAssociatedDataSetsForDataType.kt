package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Method to get the company associated dataset for a given data type
 */
fun getCompanyAssociatedDatasetsForDataType(context: Context?, dataType: String): Sequence<DataTableEntity> = sequence {
    val objectMapper = ObjectMapper()
    val getQueryResultSet = context!!.connection.createStatement().executeQuery(
        "SELECT * from data_items " +
            "WHERE data LIKE '%\\\\\\\"dataType\\\\\\\":\\\\\\\"${dataType}\\\\\\\"%'",
    )
    while (getQueryResultSet.next()) {
        val dataTableEntity = DataTableEntity(
            getQueryResultSet.getString("data_id"),
            JSONObject(
                objectMapper.readValue(
                    getQueryResultSet.getString("data"), String::class.java,
                ),
            ),
        )
        if (dataTableEntity.companyAssociatedData.getString("dataType") == dataType) {
            yield(dataTableEntity)
        }
    }
}

/**
 * Gets all data entries for a specific datatype, modifies them and writes them back to the table
 * @context the context of the migration script
 * @dataType the data type string for the data to modify
 * @migrate migration script for a single DataTableEntity
 */
fun migrateCompanyAssociatedDataOfDatatype(
    context: Context?,
    dataType: String,
    migrate: (dataTableEntity: DataTableEntity) -> Unit,
) {
    val dataTableEntities = getCompanyAssociatedDatasetsForDataType(context, dataType)
    dataTableEntities.forEach {
        migrate(it)
        context!!.connection.createStatement().execute(it.getWriteQuery())
    }
}
