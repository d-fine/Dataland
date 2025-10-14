package db.migration.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

typealias CompanyAssociatedDataPointMigration = (dataPointTableEntity: DataPointTableEntity) -> Unit

/**
 * Retrieves all data point entities of a specific type from the database.
 * @param context The Flyway migration context
 * @param dataPointType The type of data points to retrieve
 * @return List of data point entities matching the specified type
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
                dataPointId = getQueryResultSet.getString("data_point_id"),
                companyId = getQueryResultSet.getString("company_id"),
                dataPoint =
                    JSONObject(
                        objectMapper.readValue(
                            getQueryResultSet.getString("data"), String::class.java,
                        ),
                    ),
                dataPointType = dataPointType,
                reportingPeriod = getQueryResultSet.getString("reporting_period"),
                framework = getQueryResultSet.getString("framework"),
                currentlyActive = getQueryResultSet.getBoolean("currently_active"),
            ),
        )
    }

    return listOfDataPointTableEntities
}

private val logger = LoggerFactory.getLogger("Migration Iterator")

/**
 * Migrates data point entities by applying a transformation function and persisting the changes.
 * @param context The Flyway migration context
 * @param dataPointType The type of data points to migrate
 * @param migrate The transformation function to apply to each data point
 */
fun migrateDataPointTableEntities(
    context: Context?,
    dataPointType: String,
    migrate: CompanyAssociatedDataPointMigration,
) {
    val dataPointTableEntities = getDataPointTableEntitiesWithRespectToDataType(context, dataPointType)
    dataPointTableEntities.forEach {
        logger.info("Migrating $dataPointType datapoint with id: ${it.dataPointId}")
        migrate(it)
        it.executeUpdateQuery(context!!)
    }
}
