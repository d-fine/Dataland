package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import kotlin.io.path.Path
import kotlin.io.path.div

/**
 * Method to get the company associated dataset for a given data type
 */
fun getCompanyAssociatedDatasetsForDataType(context: Context?, dataType: String): Sequence<DataTableEntity> = sequence {
    val objectMapper = ObjectMapper()
    val preparedStatement = context!!.connection.prepareStatement(
        "SELECT * from data_items " +
            "WHERE data LIKE ?",
    )
    val unescapedSearchPattern = "%\"dataType\":\"$dataType\"%"
    val escapedSearchPattern = objectMapper.writeValueAsString(objectMapper.writeValueAsString(unescapedSearchPattern))
    preparedStatement.setString(1, escapedSearchPattern)
    val getQueryResultSet = preparedStatement.executeQuery()
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
        it.executeUpdateQuery(context!!)
    }
}

/**
 * Loads a json object from a json file in the resources path corresponding the migration script
 * @param version the version string prefix of the migrations class name
 * @param filename the name of the json file to load
 */
fun readJsonFromResourcesFile(version: String, filename: String): JSONObject {
    val jsonBasePath = Path("./src/test/resources/db/migration") / version
    return JSONObject((jsonBasePath / filename).toFile().readText())
}
