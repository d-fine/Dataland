package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script aligns stored data points with the updated PCAF data model:
 * - the main PCAF sector options "Sovereign" and "Industry" are replaced by "Public" and "Industrials", respectively
 *   (for both the PCAF and the SFDR sector data point type)
 * - all estimated PCAF data point types are deleted
 */
@Suppress("ClassName")
class V35__UpdatePcafSectorsAndRemoveEstimations : BaseJavaMigration() {
    companion object {
        val sectorDataPointTypes = listOf("extendedEnumPcafMainSector", "extendedEnumSfdrMainPcafSector")

        val sectorValueMap =
            mapOf(
                "Sovereign" to "Public",
                "Industry" to "Industrials",
            )

        val deletedDataPointTypes =
            listOf(
                "extendedDecimalEstimatedMarketCapitalizationInEUR",
                "extendedDecimalEstimatedBookValueOfDebtInEUR",
                "extendedDecimalEstimatedMinoritiesInterestInEUR",
                "extendedDecimalEstimatedTotalEquityAndDebtInEUR",
                "extendedDecimalEstimatedScope1GhgEmissionsInTonnes",
                "extendedDecimalEstimatedScope2GhgEmissionsLocationBasedInTonnes",
                "extendedDecimalEstimatedScope2GhgEmissionsMarketBasedInTonnes",
                "extendedDecimalEstimatedScope3GhgEmissionsInTonnes",
            )
    }

    /**
     * Replaces an obsolete sector value of a data point by its successor.
     */
    fun updateSectorValue(entity: DataPointTableEntity) {
        val currentValue = entity.dataPoint.optString("value", "")
        sectorValueMap[currentValue]?.let { entity.dataPoint.put("value", it) }
    }

    override fun migrate(context: Context?) {
        if (dataPointItemsTableExists(context)) {
            sectorDataPointTypes.forEach { migrateDataPointTableEntities(context, it, ::updateSectorValue) }
            deleteDataPointTypes(context)
        }
    }

    /**
     * The data_point_items table is created by Hibernate and therefore does not exist yet on a fresh database.
     */
    private fun dataPointItemsTableExists(context: Context?): Boolean =
        context!!
            .connection.metaData
            .getTables(null, null, "data_point_items", null)
            .use { it.next() }

    private fun deleteDataPointTypes(context: Context?) {
        context!!
            .connection
            .prepareStatement("DELETE FROM data_point_items WHERE data_point_type = ?")
            .use { statement ->
                deletedDataPointTypes.forEach { dataPointType ->
                    statement.setString(1, dataPointType)
                    statement.executeUpdate()
                }
            }
    }
}
