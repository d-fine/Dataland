package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * SFDR now uses the PCAF data point types for the main PCAF sector and the company exchange status, so that both
 * frameworks share these data points. This migration renames the SFDR-specific data point types in the QA tables to
 * the PCAF ones.
 *
 * If a company already has a data point of both types for the same reporting period, the backend keeps the SFDR data
 * point and removes the PCAF one. Accordingly, the QA information of the removed PCAF data points is deleted here
 * before the rename, so that it does not get mixed up with the QA information of the kept SFDR data points.
 */
@Suppress("ClassName")
class V22__UnifySfdrAndPcafEnumDataPointTypes : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val REVIEW_TABLE = "data_point_qa_review"
        const val REPORTS_TABLE = "data_point_qa_reports"
        const val JUDGEMENT_TABLE = "dataset_judgement_entity_data_point_judgement"

        val renameMap =
            mapOf(
                "extendedEnumSfdrMainPcafSector" to "extendedEnumPcafMainSector",
                "extendedEnumSfdrCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
            )

        /**
         * The tables containing data point types. The review table has to be the last one, as it is used to identify
         * the colliding PCAF data points.
         */
        val tablesWithDataPointType = listOf(REPORTS_TABLE, JUDGEMENT_TABLE, REVIEW_TABLE)

        private const val COLLIDING_PCAF_DATA_POINT_IDS =
            "SELECT p.data_point_id FROM $REVIEW_TABLE p WHERE p.data_point_type = ? AND EXISTS (" +
                "SELECT 1 FROM $REVIEW_TABLE s WHERE s.data_point_type = ? " +
                "AND s.company_id = p.company_id AND s.reporting_period = p.reporting_period)"

        /**
         * Builds the statement deleting the rows of the given table that belong to colliding PCAF data points.
         */
        fun deleteCollidingPcafDataPointsSql(tableName: String): String =
            "DELETE FROM $tableName WHERE data_point_type = ? AND data_point_id IN ($COLLIDING_PCAF_DATA_POINT_IDS)"
    }

    override fun migrate(context: Context?) {
        val existingTables = tablesWithDataPointType.filter { tableExists(context!!, it) }

        renameMap.forEach { (sfdrType, pcafType) ->
            if (REVIEW_TABLE in existingTables) {
                existingTables.forEach { tableName ->
                    deleteCollidingPcafDataPoints(context!!, tableName, sfdrType, pcafType)
                }
            }
            existingTables.forEach { tableName ->
                renameDataPointType(context!!, tableName, sfdrType, pcafType)
            }
        }
    }

    private fun tableExists(
        context: Context,
        tableName: String,
    ): Boolean =
        context.connection.metaData
            .getTables(null, null, tableName, null)
            .next()

    /**
     * Deletes the rows of the given table that belong to PCAF data points colliding with an SFDR data point of the
     * same company and reporting period.
     */
    fun deleteCollidingPcafDataPoints(
        context: Context,
        tableName: String,
        sfdrType: String,
        pcafType: String,
    ) {
        context.connection.prepareStatement(deleteCollidingPcafDataPointsSql(tableName)).use { statement ->
            listOf(pcafType, pcafType, sfdrType).forEachIndexed { index, parameter ->
                statement.setString(index + 1, parameter)
            }
            val deletedRows = statement.executeUpdate()
            logger.info("Deleted $deletedRows colliding \"$pcafType\" rows in $tableName")
        }
    }

    /**
     * Renames all rows matching a source data point type in the selected QA table.
     */
    fun renameDataPointType(
        context: Context,
        tableName: String,
        sourceType: String,
        targetType: String,
    ) {
        context.connection.prepareStatement("UPDATE $tableName SET data_point_type = ? WHERE data_point_type = ?").use { statement ->
            statement.setString(1, targetType)
            statement.setString(2, sourceType)
            val updatedRows = statement.executeUpdate()
            logger.info("Updated $updatedRows rows in $tableName from \"$sourceType\" to \"$targetType\"")
        }
    }
}
