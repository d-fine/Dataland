package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.getDataPointTableEntitiesWithRespectToDataType
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration to convert plain date and enum fields to extended format for fiscal year fields.
 * Handles conflicts by deleting plain data points when extended versions already exist.
 */
@Suppress("ClassName")
class V29__MigratePlainDatesToExtendedDates : BaseJavaMigration() {
    private val plainToExtendedMappings =
        mapOf(
            "plainDateFiscalYearEnd" to "extendedDateFiscalYearEnd",
            "plainEnumFiscalYearDeviation" to "extendedEnumFiscalYearDeviation",
        )

    private val relevantFrameworks = listOf("sfdr", "pcaf")

    override fun migrate(context: Context) {
        plainToExtendedMappings.forEach { (plainType, extendedType) ->
            migratePlainToExtendedTwoPass(context, plainType, extendedType)
        }
    }

    private fun migratePlainToExtendedTwoPass(
        context: Context,
        plainType: String,
        extendedType: String,
    ) {
        val conflictingTuples = findConflictingTuples(context, plainType, extendedType)

        val plainDataPoints = getDataPointTableEntitiesWithRespectToDataType(context, plainType)
        val filteredPlainDataPoints =
            plainDataPoints.filter {
                it.framework in relevantFrameworks && it.currentlyActive
            }

        filteredPlainDataPoints.forEach { dataPoint ->
            migratePlainDataPoint(context, dataPoint, extendedType, conflictingTuples)
        }
    }

    @Suppress("MagicNumber")
    private fun findConflictingTuples(
        context: Context,
        plainType: String,
        extendedType: String,
    ): Set<DataPointTuple> {
        val connection = context.connection
        val query =
            connection.prepareStatement(
                """
                SELECT DISTINCT 
                    p.company_id, 
                    p.reporting_period, 
                    p.framework
                FROM data_point_items p
                INNER JOIN data_point_items e 
                    ON p.company_id = e.company_id 
                    AND p.reporting_period = e.reporting_period
                    AND p.framework = e.framework
                WHERE p.data_point_type = ?
                    AND e.data_point_type = ?
                    AND p.framework IN (?, ?)
                    AND p.currently_active = true
                    AND e.currently_active = true
                """.trimIndent(),
            )

        query.setString(1, plainType)
        query.setString(2, extendedType)
        query.setString(3, relevantFrameworks[0])
        query.setString(4, relevantFrameworks[1])

        val results = query.executeQuery()
        val conflicts = mutableSetOf<DataPointTuple>()

        while (results.next()) {
            conflicts.add(
                DataPointTuple(
                    companyId = results.getString("company_id"),
                    reportingPeriod = results.getString("reporting_period"),
                    framework = results.getString("framework"),
                ),
            )
        }

        results.close()
        query.close()

        return conflicts
    }

    private fun migratePlainDataPoint(
        context: Context,
        dataPoint: DataPointTableEntity,
        extendedType: String,
        conflictingTuples: Set<DataPointTuple>,
    ) {
        val tuple =
            DataPointTuple(
                companyId = dataPoint.companyId,
                reportingPeriod = dataPoint.reportingPeriod,
                framework = dataPoint.framework,
            )

        if (tuple in conflictingTuples) {
            deleteDataPoint(context, dataPoint)
        } else {
            dataPoint.dataPointType = extendedType
            dataPoint.executeUpdateQuery(context)
        }
    }

    @Suppress("MagicNumber")
    private fun deleteDataPoint(
        context: Context,
        dataPoint: DataPointTableEntity,
    ) {
        val deleteStatement =
            context.connection.prepareStatement(
                "DELETE FROM data_point_items WHERE data_point_id = ?",
            )
        deleteStatement.setString(1, dataPoint.dataPointId)
        deleteStatement.executeUpdate()
        deleteStatement.close()
    }

    /**
     * Represents a unique combination of company, reporting period, and framework
     * for identifying conflicting data points.
     */
    data class DataPointTuple(
        val companyId: String,
        val reportingPeriod: String,
        val framework: String,
    )

    /**
     * Converts a plain data point to extended format by updating its data point type.
     * @param dataPointTableEntity The data point entity to convert
     * @param extendedType The new extended type name
     */
    fun convertPlainToExtended(
        dataPointTableEntity: DataPointTableEntity,
        extendedType: String,
    ) {
        dataPointTableEntity.dataPointType = extendedType
    }
}
