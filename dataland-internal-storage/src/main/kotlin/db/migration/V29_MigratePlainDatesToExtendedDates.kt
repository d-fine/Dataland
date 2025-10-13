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
        val plainDatapoints = queryPlainDatapoints(connection, plainType)
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
        dataPointsToProcess.forEach { plainDataPoint ->
            processPlainDataPoint(connection, plainDataPoint, extendedType, deleteStatement, updateStatement)
        }
        deleteStatement.close()
        updateStatement.close()
    }

    private fun queryPlainDatapoints(
        connection: java.sql.Connection,
        plainType: String,
    ): java.sql.ResultSet {
        val plainDatapointsQuery =
            connection.prepareStatement(
                "SELECT data_point_id, company_id, reporting_period, framework " +
                    "FROM data_point_items " +
                    "WHERE data_point_type = ? AND framework IN (?, ?) AND currently_active = true",
            )
        val params = listOf(plainType) + relevantFrameworks
        for ((idx, value) in params.withIndex()) {
            plainDatapointsQuery.setString(idx + 1, value)
        }
        return plainDatapointsQuery.executeQuery()
    }

    private fun processPlainDataPoint(
        connection: java.sql.Connection,
        plainDataPoint: DataPointInfo,
        extendedType: String,
        deleteStatement: java.sql.PreparedStatement,
        updateStatement: java.sql.PreparedStatement,
    ) {
        val checkExtendedQuery =
            connection.prepareStatement(
                "SELECT COUNT(*) as count FROM data_point_items " +
                    "WHERE company_id = ? AND reporting_period = ? AND framework = ? " +
                    "AND data_point_type = ? AND currently_active = true",
            )
        val params =
            listOf(
                plainDataPoint.companyId,
                plainDataPoint.reportingPeriod,
                plainDataPoint.framework,
                extendedType,
            )
        for ((idx, value) in params.withIndex()) {
            checkExtendedQuery.setString(idx + 1, value)
        }
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

    private data class DataPointInfo(
        val dataPointId: String,
        val companyId: String,
        val reportingPeriod: String,
        val framework: String,
    )
}
