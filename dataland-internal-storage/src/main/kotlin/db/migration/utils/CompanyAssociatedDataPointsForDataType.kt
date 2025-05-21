package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

typealias CompanyAssociatedDataPointMigration = (dataPointTableEntity: DataPointTableEntity) -> Unit

/**
 * Method to get the company associated data point for a given data point type
 */

fun getCompanyAssociatedDatapointsForDataType(
    context: Context?,
    dataPointType: String,
): List<DataPointTableEntity> {
    val objectMapper = ObjectMapper()
    val preparedStatement =
        context!!.connection.prepareStatement(
            "SELECT * from data_point_items WHERE data_point_type = ?",
        )
    preparedStatement.setString(1, "%dataPointType%")
    val getQueryResultSet = preparedStatement.executeQuery()

    val companyAssociatedDatapoints = mutableListOf<DataPointTableEntity>()
    while (getQueryResultSet.next()) {
        companyAssociatedDatapoints.add(
            DataPointTableEntity(
                getQueryResultSet.getString("data_point_id"),
                JSONObject(getQueryResultSet.getString("data")),
                dataPointType,
                getQueryResultSet.getString("reporting_period"),
            ),
        )
    }

    return companyAssociatedDatapoints.filter { dataPointTableEntity ->
        dataPointTableEntity.dataPointType == dataPointType
    }
}

private val logger = LoggerFactory.getLogger("Migration Iterator")

/**
 * Gets all data entries for a specific datatype, modifies them and writes them back to the table
 * @context the context of the migration script
 * @dataType the data type string for the data to modify
 * @migrate migration script for a single DataTableEntity
 */
fun migrateCompanyAssociatedDatapointOfDatatype(
    context: Context?,
    dataPointType: String,
    migrate: CompanyAssociatedDataPointMigration,
) {
    val dataPointTableEntities = getCompanyAssociatedDatapointsForDataType(context, dataPointType)
    dataPointTableEntities.forEach {
        logger.info("Migrating $dataPointType datapoint with id: ${it.dataPointId}")
        migrate(it)
        it.executeUpdateQuery(context!!)
    }
}
