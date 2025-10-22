package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Migration to convert plain date and enum fields to extended format for fiscal year fields.
 * Handles conflicts where both plain and extended versions exist by deactivating the plain version.
 */
@Suppress("ClassName", "MagicNumber")
class V10__MigratePlainDatesToExtendedDates : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val plainToExtendedMappings =
        mapOf(
            "plainDateFiscalYearEnd" to "extendedDateFiscalYearEnd",
            "plainEnumFiscalYearDeviation" to "extendedEnumFiscalYearDeviation",
        )

    override fun migrate(context: Context?) {
        val metaTable = "data_point_meta_information"
        val uuidTable = "data_point_uuid_map"
        val typeColumn = "data_point_type"
        val identifierColumn = "data_point_identifier"

        val metaResultSet = context!!.connection.metaData.getTables(null, null, metaTable, null)
        val uuidResultSet = context.connection.metaData.getTables(null, null, uuidTable, null)

        if (metaResultSet.next()) {
            plainToExtendedMappings.forEach { (plainType, extendedType) ->
                val conflicts =
                    findConflictingTuples(
                        context,
                        metaTable,
                        typeColumn,
                        plainType,
                        extendedType,
                    )

                if (conflicts.isNotEmpty()) {
                    logger.info("Found ${conflicts.size} conflicts for $plainType, deactivating plain versions")
                    deactivateConflictingPlainDataPoints(
                        context,
                        metaTable,
                        typeColumn,
                        plainType,
                        conflicts,
                    )
                }

                migratePlainToExtended(
                    context,
                    metaTable,
                    typeColumn,
                    plainType,
                    extendedType,
                )
            }
        }

        if (uuidResultSet.next()) {
            plainToExtendedMappings.forEach { (plainType, extendedType) ->
                migratePlainToExtended(
                    context,
                    uuidTable,
                    identifierColumn,
                    plainType,
                    extendedType,
                )
            }
        }
    }

    /**
     * Finds tuples (company_id + reporting_period) where both a plain and extended
     * data point exist and are active.
     *
     * @param context the Flyway context
     * @param tableName name of the table to query
     * @param columnName type/identifier column
     * @param plainType the plain value to migrate from
     * @param extendedType the extended value to migrate to
     * @return a set of conflicting DataPointTuples
     */
    fun findConflictingTuples(
        context: Context,
        tableName: String,
        columnName: String,
        plainType: String,
        extendedType: String,
    ): Set<DataPointTuple> {
        val query =
            context.connection.prepareStatement(
                """
                SELECT DISTINCT
                    p.company_id,
                    p.reporting_period
                FROM $tableName p
                INNER JOIN $tableName e
                    ON p.company_id = e.company_id
                    AND p.reporting_period = e.reporting_period
                WHERE p.$columnName = ?
                    AND e.$columnName = ?
                    AND p.currently_active = true
                    AND e.currently_active = true
                """.trimIndent(),
            )

        query.setString(1, plainType)
        query.setString(2, extendedType)

        val results = query.executeQuery()
        val conflicts = mutableSetOf<DataPointTuple>()

        while (results.next()) {
            conflicts.add(
                DataPointTuple(
                    companyId = results.getString("company_id"),
                    reportingPeriod = results.getString("reporting_period"),
                ),
            )
        }

        results.close()
        query.close()

        return conflicts
    }

    /**
     * Deactivates plain data points that conflict with extended versions.
     *
     * @param context the Flyway context
     * @param tableName name of the table to update
     * @param columnName type/identifier column
     * @param plainType the plain value to deactivate
     * @param conflicts set of tuples to deactivate
     * @return count of deactivated rows
     */
    fun deactivateConflictingPlainDataPoints(
        context: Context,
        tableName: String,
        columnName: String,
        plainType: String,
        conflicts: Set<DataPointTuple>,
    ): Int {
        val updateStatement =
            context.connection.prepareStatement(
                """
                UPDATE $tableName
                SET currently_active = null
                WHERE $columnName = ?
                    AND company_id = ?
                    AND reporting_period = ?
                    AND currently_active = true
                """.trimIndent(),
            )

        var totalDeactivated = 0

        conflicts.forEach { conflict ->
            updateStatement.setString(1, plainType)
            updateStatement.setString(2, conflict.companyId)
            updateStatement.setString(3, conflict.reportingPeriod)
            totalDeactivated += updateStatement.executeUpdate()
        }

        updateStatement.close()
        logger.info("Deactivated $totalDeactivated plain data points in $tableName for $plainType")

        return totalDeactivated
    }

    /**
     * Migrates plain data point types to extended format.
     *
     * @param context the Flyway context
     * @param tableName name of the table to update
     * @param columnName type/identifier column
     * @param plainType the plain value to migrate from
     * @param extendedType the extended value to migrate to
     */
    fun migratePlainToExtended(
        context: Context,
        tableName: String,
        columnName: String,
        plainType: String,
        extendedType: String,
    ) {
        val updateStatement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET $columnName = ? WHERE $columnName = ?",
            )

        updateStatement.setString(1, extendedType)
        updateStatement.setString(2, plainType)
        val count = updateStatement.executeUpdate()

        updateStatement.close()
        logger.info("Migrated $count rows in $tableName from $plainType to $extendedType")
    }

    /**
     * Simple data class to represent a (company_id, reporting_period) tuple
     */
    data class DataPointTuple(
        val companyId: String,
        val reportingPeriod: String,
    )
}
