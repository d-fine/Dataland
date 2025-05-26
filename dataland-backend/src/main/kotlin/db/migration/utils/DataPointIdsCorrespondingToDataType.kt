package db.migration.utils

import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

typealias DataPointIdAndDataPointTypeMigration = (entity: DataPointIdAndDataPointTypeEntity) -> Unit

/**
 * Method to get the all tuples of a data point id and data point type for a fixed data point type
 */

fun getDataPointIdsCorrespondingToDataType(
    context: Context?,
    dataPointType: String,
    tableName: String,
    columnName: String,
): List<DataPointIdAndDataPointTypeEntity> {
    val preparedStatement =
        context!!.connection.prepareStatement(
            "SELECT * from $tableName WHERE $columnName = ?",
        )
    preparedStatement.setString(1, dataPointType)
    val getQueryResultSet = preparedStatement.executeQuery()

    val dataIdsAssociatedToDataPoint = mutableListOf<DataPointIdAndDataPointTypeEntity>()
    while (getQueryResultSet.next()) {
        dataIdsAssociatedToDataPoint.add(
            DataPointIdAndDataPointTypeEntity(
                getQueryResultSet.getString("data_point_id"),
                getQueryResultSet.getString("data_point_type"),
            ),
        )
    }

    return dataIdsAssociatedToDataPoint
}

private val logger = LoggerFactory.getLogger("Migration Iterator")

/**
 * Migrates all tuples of data point ids and data point types for a fixed data point type
 * @context the context of the migration script
 * @dataPointType the data point type for the tuples to modify
 * @migrate migration script for a tuple of data point id and data point type
 */

fun migrateBackendTable(
    context: Context?,
    dataPointType: String,
    tableName: String,
    columnName: String,
    migrate: DataPointIdAndDataPointTypeMigration,
) {
    val dataPointTableEntities = getDataPointIdsCorrespondingToDataType(context, dataPointType, tableName, columnName)
    dataPointTableEntities.forEach {
        logger.info("Migrating $dataPointType data point with id: ${it.dataPointId} in $tableName")
        migrate(it)
        it.executeUpdateQuery(context!!)
    }
}
