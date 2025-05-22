package db.migration.utils
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

typealias CompanyAssociatedDataPointMigration = (entity: DataPointIdAndDataPointTypeEntity) -> Unit

/**
 * Method to get the company associated data point for a given data point type
 */

fun getDataPointIdsCorrespondingToDataType(
    context: Context?,
    dataPointType: String,
): List<DataPointIdAndDataPointTypeEntity> {
    val preparedStatement =
        context!!.connection.prepareStatement(
            "SELECT * from public.data_point_meta_information WHERE data_point_type = ?",
        )
    preparedStatement.setString(1, dataPointType)
    val getQueryResultSet = preparedStatement.executeQuery()

    val dataIdsAssociatedToDataPoint = mutableListOf<DataPointIdAndDataPointTypeEntity>()
    while (getQueryResultSet.next()) {
        dataIdsAssociatedToDataPoint.add(
            DataPointIdAndDataPointTypeEntity(
                getQueryResultSet.getString("data_point_id"),
                getQueryResultSet.getString("data_point_id"),
            ),
        )
    }

    return dataIdsAssociatedToDataPoint
}

private val logger = LoggerFactory.getLogger("Migration Iterator")

/**
 * Gets all data entries for a specific datatype, modifies them and writes them back to the table
 * @context the context of the migration script
 * @dataType the data type string for the data to modify
 * @migrate migration script for a single DataTableEntity
 */

fun migrateDataPointIdsAndDataPointTypes(
    context: Context?,
    dataPointType: String,
    migrate: CompanyAssociatedDataPointMigration,
) {
    val dataPointTableEntities = getDataPointIdsCorrespondingToDataType(context, dataPointType)
    dataPointTableEntities.forEach {
        logger.info("Migrating $dataPointType datapoint with id: ${it.dataPointId}")
        migrate(it)
        it.executeUpdateQuery(context!!)
    }
}
