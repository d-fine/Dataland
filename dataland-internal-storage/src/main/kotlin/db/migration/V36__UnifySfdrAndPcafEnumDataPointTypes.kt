package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * SFDR now uses the PCAF data point types for the main PCAF sector and the company exchange status, so that both
 * frameworks share these data points. This migration renames the SFDR-specific data point types in the
 * data_point_items table to the PCAF ones. The values are not touched, as the options of both types are identical.
 *
 * If a company already has a data point of both types for the same reporting period, the SFDR data point is kept and
 * the PCAF data point is deleted beforehand.
 */
@Suppress("ClassName")
class V36__UnifySfdrAndPcafEnumDataPointTypes : BaseJavaMigration() {
    companion object {
        val renameMap =
            mapOf(
                "extendedEnumSfdrMainPcafSector" to "extendedEnumPcafMainSector",
                "extendedEnumSfdrCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
            )

        const val DELETE_COLLIDING_PCAF_DATA_POINTS_SQL =
            "DELETE FROM data_point_items p WHERE p.data_point_type = ? AND EXISTS (" +
                "SELECT 1 FROM data_point_items s WHERE s.data_point_type = ? " +
                "AND s.company_id = p.company_id AND s.reporting_period = p.reporting_period)"

        const val RENAME_SQL = "UPDATE data_point_items SET data_point_type = ? WHERE data_point_type = ?"
    }

    override fun migrate(context: Context?) {
        if (!dataPointItemsTableExists(context!!)) return
        renameMap.forEach { (sfdrType, pcafType) ->
            context.connection.prepareStatement(DELETE_COLLIDING_PCAF_DATA_POINTS_SQL).use { statement ->
                statement.setString(1, pcafType)
                statement.setString(2, sfdrType)
                statement.executeUpdate()
            }
            context.connection.prepareStatement(RENAME_SQL).use { statement ->
                statement.setString(1, pcafType)
                statement.setString(2, sfdrType)
                statement.executeUpdate()
            }
        }
    }

    /**
     * The data_point_items table is created by Hibernate and therefore does not exist yet on a fresh database.
     */
    private fun dataPointItemsTableExists(context: Context): Boolean =
        context.connection.metaData
            .getTables(null, null, "data_point_items", null)
            .use { it.next() }
}
