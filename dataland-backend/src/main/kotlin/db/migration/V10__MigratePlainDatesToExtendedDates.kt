package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Migration to convert plain date and enum fields to extended format for fiscal year fields.
 * Handles conflicts by deleting plain data points when extended versions already exist.
 */
@Suppress("ClassName")
class V10__MigratePlainDatesToExtendedDates : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val plainToExtendedMappings =
        mapOf(
            "plainDateFiscalYearEnd" to "extendedDateFiscalYearEnd",
            "plainEnumFiscalYearDeviation" to "extendedEnumFiscalYearDeviation",
        )

    private val relevantFrameworks = listOf("sfdr", "pcaf")

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
     * @param context the context of the migration script
     * @param tableName the name of the table
     * @param columnName the name of the column containing the data point type
     * @param plainType the plain data point type to migrate from
     * @param extendedType the extended data point type to migrate to
     */
    fun migratePlainToExtended(
        context: Context,
        tableName: String,
        columnName: String,
        plainType: String,
        extendedType: String,
    ) {
        val conflictingTuples = findConflictingTuples(context, tableName, columnName, plainType, extendedType)

        if (conflictingTuples.isNotEmpty()) {
            val deleteCount =
                deleteConflictingPlainDataPoints(
                    context,
                    tableName,
                    columnName,
                    plainType,
                    conflictingTuples,
                )
            logger.info("Deleted $deleteCount conflicting plain data points of type $plainType from $tableName")
        }

        val updateStatement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET $columnName = ? " +
                    "WHERE $columnName = ? AND framework IN (?, ?) AND currently_active = true",
            )
        updateStatement.setString(1, extendedType)
        updateStatement.setString(2, plainType)
        @Suppress("MagicNumber")
        updateStatement.setString(3, relevantFrameworks[0])
        @Suppress("MagicNumber")
        updateStatement.setString(4, relevantFrameworks[1])
        val count = updateStatement.executeUpdate()
        logger.info("Updated $count rows in $tableName from $plainType to $extendedType")

        updateStatement?.close()
    }

    /**
     * Finds tuples where both plain and extended data point types exist.
     * @param context the context of the migration script
     * @param tableName the name of the table
     * @param columnName the name of the column containing the data point type
     * @param plainType the plain data point type
     * @param extendedType the extended data point type
     * @return set of conflicting tuples (company_id, reporting_period, framework)
     */
    @Suppress("MagicNumber")
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
                    p.reporting_period,
                    p.framework
                FROM $tableName p
                INNER JOIN $tableName e
                    ON p.company_id = e.company_id
                    AND p.reporting_period = e.reporting_period
                    AND p.framework = e.framework
                WHERE p.$columnName = ?
                    AND e.$columnName = ?
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

    /**
     * Deletes plain data points that conflict with existing extended data points.
     * @param context the context of the migration script
     * @param tableName the name of the table
     * @param columnName the name of the column containing the data point type
     * @param plainType the plain data point type to delete
     * @param conflictingTuples the set of conflicting tuples
     * @return number of deleted rows
     */
    @Suppress("MagicNumber")
    fun deleteConflictingPlainDataPoints(
        context: Context,
        tableName: String,
        columnName: String,
        plainType: String,
        conflictingTuples: Set<DataPointTuple>,
    ): Int {
        var totalDeleted = 0
        val deleteStatement =
            context.connection.prepareStatement(
                "DELETE FROM $tableName WHERE $columnName = ? AND company_id = ? " +
                    "AND reporting_period = ? AND framework = ? AND currently_active = true",
            )

        conflictingTuples.forEach { tuple ->
            deleteStatement.setString(1, plainType)
            deleteStatement.setString(2, tuple.companyId)
            deleteStatement.setString(3, tuple.reportingPeriod)
            deleteStatement.setString(4, tuple.framework)
            totalDeleted += deleteStatement.executeUpdate()
        }

        deleteStatement.close()
        return totalDeleted
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
}
