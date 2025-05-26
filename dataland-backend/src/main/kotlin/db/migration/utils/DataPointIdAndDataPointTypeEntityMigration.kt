package db.migration.utils

import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

typealias Migration = (entity: DataPointIdAndDataPointTypeEntity) -> Unit

/**
 * Migrating Data Point records in database during Flyway migration.
 */
class DataPointIdAndDataPointTypeMigration {
    private val logger = LoggerFactory.getLogger("Migration Iterator")

    /**
     * Migrates all tuples of data point ids and data point types for a fixed data point type
     * @context the context of the migration script
     * @dataPointType the data point type for the tuples to modify
     * @migrate migration script for a tuple of data point id and data point type
     */

    fun migrateDataPointIdsAndDataPointTypes(
        context: Context?,
        dataPointType: String,
        migrate: Migration,
    ) {
        val dataPointTableEntities = getDataPointIdsCorrespondingToDataType(context, dataPointType)
        dataPointTableEntities.forEach {
            logger.info("Migrating $dataPointType datapoint with id: ${it.dataPointId}")
            migrate(it)
            it.executeUpdateQuery(context!!)
        }
    }
}
