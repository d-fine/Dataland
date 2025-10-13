package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration consolidates fiscal year data points by migrating plainDateFiscalYearEnd
 * and plainEnumFiscalYearDeviation to their extended counterparts (extendedDateFiscalYearEnd
 * and extendedEnumFiscalYearDeviation) across SFDR and PCAF frameworks.
 *
 * For each (company, reporting_period, framework) tuple:
 * - If both plain and extended currently_active data points exist, keep extended and delete plain
 * - If only plain currently_active exists, convert it to extended
 * - Non-active data points are left as-is
 */
@Suppress("ClassName")
class V29_MigratePlainDatesToExtendedDates : BaseJavaMigration() {
    private val plainToExtendedMappings =
        mapOf(
            "plainDateFiscalYearEnd" to "extendedDateFiscalYearEnd",
            "plainEnumFiscalYearDeviation" to "extendedEnumFiscalYearDeviation",
        )

    private val relevantFrameworks = listOf("sfdr", "pcaf")

    override fun migrate(context: Context) {
        plainToExtendedMappings.forEach { (plainType, extendedType) ->
            migratePlainToExtended(context, plainType, extendedType)
        }
    }

    /**
     * Migrates plain data points to extended data points for SFDR and PCAF frameworks.
     * Only processes currently_active data points.
     * If both plain and extended currently_active exist: delete plain (prefer extended)
     * If only plain currently_active exists: cast it to extended type
     */
    private fun migratePlainToExtended(
        context: Context,
        plainType: String,
        extendedType: String,
    ) {
        val connection = context.connection

        val plainDatapointsQuery =
            connection.prepareStatement(
                "SELECT data_point_id, company_id, reporting_period, framework " +
                    "FROM data_point_items " +
                    "WHERE data_point_type = ? AND framework IN (?, ?) AND currently_active = true",
            )
        plainDatapointsQuery.setString(1, plainType)
        plainDatapointsQuery.setString(2, relevantFrameworks[0])
        plainDatapointsQuery.setString(3, relevantFrameworks[1])
        val plainDatapoints = plainDatapointsQuery.executeQuery()

        val deleteStatement =
            connection.prepareStatement(
                "DELETE FROM data_point_items WHERE data_point_id = ?",
            )

        val updateStatement =
            connection.prepareStatement(
                "UPDATE data_point_items SET data_point_type = ? WHERE data_point_id = ?",
            )

        val dataPointsToProcess = mutableListOf<DataPointInfo>()
        while (plainDatapoints.next()) {
            dataPointsToProcess.add(
                DataPointInfo(
                    dataPointId = plainDatapoints.getString("data_point_id"),
                    companyId = plainDatapoints.getString("company_id"),
                    reportingPeriod = plainDatapoints.getString("reporting_period"),
                    framework = plainDatapoints.getString("framework"),
                ),
            )
        }
        plainDatapoints.close()
        plainDatapointsQuery.close()

        dataPointsToProcess.forEach { plainDataPoint ->
            val checkExtendedQuery =
                connection.prepareStatement(
                    "SELECT COUNT(*) as count FROM data_point_items " +
                        "WHERE company_id = ? AND reporting_period = ? AND framework = ? " +
                        "AND data_point_type = ? AND currently_active = true",
                )
            checkExtendedQuery.setString(1, plainDataPoint.companyId)
            checkExtendedQuery.setString(2, plainDataPoint.reportingPeriod)
            checkExtendedQuery.setString(3, plainDataPoint.framework)
            checkExtendedQuery.setString(4, extendedType)
            val extendedResult = checkExtendedQuery.executeQuery()

            extendedResult.next()
            val extendedExists = extendedResult.getInt("count") > 0

            extendedResult.close()
            checkExtendedQuery.close()

            if (extendedExists) {
                deleteStatement.setString(1, plainDataPoint.dataPointId)
                deleteStatement.executeUpdate()
            } else {
                updateStatement.setString(1, extendedType)
                updateStatement.setString(2, plainDataPoint.dataPointId)
                updateStatement.executeUpdate()
            }
        }

        deleteStatement.close()
        updateStatement.close()
    }

    private data class DataPointInfo(
        val dataPointId: String,
        val companyId: String,
        val reportingPeriod: String,
        val framework: String,
    )
}
