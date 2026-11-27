package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * SFDR now uses the PCAF data point types for the main PCAF sector and the company exchange status, so that both
 * frameworks share these data points. This migration renames the SFDR-specific data point types in the backend's
 * data point metadata tables (data_point_meta_information, data_point_uuid_map) to the PCAF ones.
 *
 * If a company already has a data point of both types for the same reporting period, the SFDR data point is kept and
 * the PCAF data point is removed beforehand, as two data points of the same type would collide after the rename.
 */
@Suppress("ClassName")
class V19__UnifySfdrAndPcafEnumDataPointTypes : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val META_TABLE = "data_point_meta_information"
        const val UUID_TABLE = "data_point_uuid_map"

        val renameMap =
            mapOf(
                "extendedEnumSfdrMainPcafSector" to "extendedEnumPcafMainSector",
                "extendedEnumSfdrCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
            )

        private const val COLLIDING_PCAF_DATA_POINT_IDS =
            "SELECT p.data_point_id FROM $META_TABLE p WHERE p.data_point_type = ? AND EXISTS (" +
                "SELECT 1 FROM $META_TABLE s WHERE s.data_point_type = ? " +
                "AND s.company_id = p.company_id AND s.reporting_period = p.reporting_period)"

        const val DELETE_COLLIDING_UUID_MAP_ENTRIES_SQL =
            "DELETE FROM $UUID_TABLE WHERE data_point_uuid IN ($COLLIDING_PCAF_DATA_POINT_IDS)"

        const val DELETE_COLLIDING_META_INFORMATION_SQL =
            "DELETE FROM $META_TABLE WHERE data_point_id IN ($COLLIDING_PCAF_DATA_POINT_IDS)"
    }

    override fun migrate(context: Context?) {
        val metaTableExists = tableExists(context!!, META_TABLE)
        val uuidTableExists = tableExists(context, UUID_TABLE)

        renameMap.forEach { (sfdrType, pcafType) ->
            if (metaTableExists) {
                if (uuidTableExists) {
                    deleteCollidingPcafDataPoints(context, DELETE_COLLIDING_UUID_MAP_ENTRIES_SQL, sfdrType, pcafType)
                }
                deleteCollidingPcafDataPoints(context, DELETE_COLLIDING_META_INFORMATION_SQL, sfdrType, pcafType)
                renameDataPointType(context, META_TABLE, "data_point_type", sfdrType, pcafType)
            }
            if (uuidTableExists) {
                renameDataPointType(context, UUID_TABLE, "data_point_identifier", sfdrType, pcafType)
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
     * Executes the given delete statement for all PCAF data points that collide with an SFDR data point of the
     * same company and reporting period.
     */
    fun deleteCollidingPcafDataPoints(
        context: Context,
        deleteSql: String,
        sfdrType: String,
        pcafType: String,
    ) {
        context.connection.prepareStatement(deleteSql).use { statement ->
            statement.setString(1, pcafType)
            statement.setString(2, sfdrType)
            val deletedRows = statement.executeUpdate()
            logger.info("Deleted $deletedRows colliding \"$pcafType\" rows using: $deleteSql")
        }
    }

    /**
     * Renames all rows matching a source data point type in the selected table and column.
     */
    fun renameDataPointType(
        context: Context,
        tableName: String,
        columnName: String,
        sourceType: String,
        targetType: String,
    ) {
        context.connection.prepareStatement("UPDATE $tableName SET $columnName = ? WHERE $columnName = ?").use { statement ->
            statement.setString(1, targetType)
            statement.setString(2, sourceType)
            val updatedRows = statement.executeUpdate()
            logger.info("Updated $updatedRows rows in $tableName from \"$sourceType\" to \"$targetType\"")
        }
    }
}
