package db.migration.utils

import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

typealias DataPointMigration = (dataPointTableEntity: DataPointTableEntity) -> Unit

/**
 * Migrating DataPointTableEntity records in database during Flyway migration.
 * Provides methods for updating data point types and purging legacy fields.
 */
class DataPointTableEntityMigration {
    private val logger = LoggerFactory.getLogger("Migration Iterator")

    /**
     * Gets all data entries for a specific datatype, modifies them and writes them back to the table
     * @context the context of the migration script
     * @dataType the data type string for the data to modify
     * @migrate migration script for a single DataTableEntity
     */
    fun migrateDataPointTableEntities(
        context: Context?,
        dataPointType: String,
        migrate: DataPointMigration,
    ) {
        val dataPointTableEntities = getDataPointTableEntitiesWithRespectToDataType(context, dataPointType)
        dataPointTableEntities.forEach {
            logger.info("Migrating $dataPointType datapoint with id: ${it.dataPointId}")
            migrate(it)
            it.executeUpdateQuery(context!!)
        }
    }
}
