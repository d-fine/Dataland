package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

typealias CompanyAssociatedDataMigration = (dataTableEntity: DataTableEntity) -> Unit

/**
 * Method to get the company associated dataset for a given data type
 */
fun getCompanyAssociatedDatasetsForDataType(
    context: Context?,
    dataType: String,
): List<DataTableEntity> {
    val objectMapper = ObjectMapper()
    val preparedStatement =
        context!!.connection.prepareStatement(
            "SELECT * from data_items " +
                "WHERE data LIKE '%\\\\\\\"dataType\\\\\\\":\\\\\\\"${dataType}\\\\\\\"%'",
        )
    val getQueryResultSet = preparedStatement.executeQuery()

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

    return companyAssociatedDatasets.filter { dataTableEntity ->
        dataTableEntity.companyAssociatedData.getString("dataType") == dataType
    }
}

private val logger = LoggerFactory.getLogger("Migration Iterator")

/**
 * Gets all data entries for a specific datatype, modifies them and writes them back to the table
 * @context the context of the migration script
 * @dataType the data type string for the data to modify
 * @migrate migration script for a single DataTableEntity
 */
fun migrateCompanyAssociatedDataOfDatatype(
    context: Context?,
    dataType: String,
    migrate: CompanyAssociatedDataMigration,
) {
    val dataTableEntities = getCompanyAssociatedDatasetsForDataType(context, dataType)
    dataTableEntities.forEach {
        logger.info("Migrating $dataType dataset with id: ${it.dataId}")
        migrate(it)
        it.executeUpdateQuery(context!!)
    }
}
