package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Migration to convert plain date and enum fields to extended format for fiscal year fields.
 * Handles conflicts by deactivating plain data points when extended versions already exist.
 */
@Suppress("ClassName")
class V10__MigratePlainDatesToExtendedDates : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val plainToExtendedMappings =
        mapOf(
            "plainDateFiscalYearEnd" to "extendedDateFiscalYearEnd",
            "plainEnumFiscalYearDeviation" to "extendedEnumFiscalYearDeviation",
        )

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val metaTable = "data_point_meta_information"
        val uuidTable = "data_point_uuid_map"
        val typeColumn = "data_point_type"
        val identifierColumn = "data_point_identifier"

        val metaResultSet = connection.metaData.getTables(null, null, metaTable, null)
        val uuidResultSet = connection.metaData.getTables(null, null, uuidTable, null)

        if (metaResultSet.next()) {
            plainToExtendedMappings.forEach { (plainType, extendedType) ->
                migratePlainToExtended(context, metaTable, typeColumn, plainType, extendedType)
            }
        }

        if (uuidResultSet.next()) {
            plainToExtendedMappings.forEach { (plainType, extendedType) ->
                migratePlainToExtended(context, uuidTable, identifierColumn, plainType, extendedType)
            }
        }
    }

    /**
     * Migrates plain data point types to extended format with conflict handling.
     *
     * Steps:
     * 1. Detect conflicts where plain and extended both exist.
     * 2. Deactivate the plain version in those conflicts.
     * 3. Update remaining active plain records to extended type.
     *
     * @param context the Flyway migration context
     * @param tableName the name of the target table
     * @param columnName the name of the type/identifier column
     * @param plainType the original type name
     * @param extendedType the new type name
     */
    @Suppress("MagicNumber")
    fun migratePlainToExtended(
        context: Context,
        tableName: String,
        columnName: String,
        plainType: String,
        extendedType: String,
    ) {
        val conflictingTuples = findConflictingTuples(context, tableName, columnName, plainType, extendedType)

        if (conflictingTuples.isNotEmpty()) {
            val deactivateCount =
                deactivateConflictingPlainDataPoints(
                    context,
                    tableName,
                    columnName,
                    plainType,
                    conflictingTuples,
                )
            logger.info("Deactivated $deactivateCount conflicting plain data points of type $plainType in $tableName")
        }

        val updateStatement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET $columnName = ? " +
                    "WHERE $columnName = ? AND currently_active = true",
            )
        updateStatement.setString(1, extendedType)
        updateStatement.setString(2, plainType)

        val count = updateStatement.executeUpdate()
        logger.info("Updated $count rows in $tableName from $plainType to $extendedType")

        updateStatement.close()
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
     * Deactivates all plain data points that conflict with already-active extended ones.
     *
     * @param context Flyway context
     * @param tableName name of the table
     * @param columnName type/identifier column
     * @param plainType value to deactivate
     * @param conflictingTuples list of companyId/reportingPeriod pairs
     * @return total number of deactivated records
     */
    @Suppress("MagicNumber")
    fun deactivateConflictingPlainDataPoints(
        context: Context,
        tableName: String,
        columnName: String,
        plainType: String,
        conflictingTuples: Set<DataPointTuple>,
    ): Int {
        var totalDeactivated = 0
        val deactivateStatement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET currently_active = null " +
                    "WHERE $columnName = ? AND company_id = ? " +
                    "AND reporting_period = ? AND currently_active = true",
            )

        conflictingTuples.forEach { tuple ->
            deactivateStatement.setString(1, plainType)
            deactivateStatement.setString(2, tuple.companyId)
            deactivateStatement.setString(3, tuple.reportingPeriod)
            totalDeactivated += deactivateStatement.executeUpdate()
        }

        deactivateStatement.close()
        return totalDeactivated
    }

    /**
     * Tuple representing (company_id, reporting_period) for conflict resolution.
     */
    data class DataPointTuple(
        val companyId: String,
        val reportingPeriod: String,
    )
}
