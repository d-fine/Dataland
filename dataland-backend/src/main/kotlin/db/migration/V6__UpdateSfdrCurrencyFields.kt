package db.migration

import db.migration.utils.DataPointIdAndDataPointTypeEntity
import db.migration.utils.migrateDataPointIdsAndDataPointTypes
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates the meta information of currency related Sfdr data points, and corrects a suffix in the
 * carbon footprint.
 */
@Suppress("ClassName")
class V6__UpdateSfdrCurrencyFields : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateDataPointIdsAndDataPointTypes(
            context,
            "extendedCurrencyTotalRevenue",
        ) { this.updateRespectiveDataType(it) }

        migrateDataPointIdsAndDataPointTypes(
            context,
            "extendedCurrencyEnterpriseValue",
            this::updateRespectiveDataType,
        )

        migrateDataPointIdsAndDataPointTypes(
            context,
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
            this::updateRespectiveDataType,
        )
    }

    val renameMap =
        mapOf(
            "extendedCurrencyTotalRevenue" to "extendedDecimalTotalRevenueInEUR",
            "extendedCurrencyEnterpriseValue" to "extendedDecimalEnterpriseValueInEUR",
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
                to "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
        )

    /**
     * Updates the meta information of currency-related Sfdr data points, and corrects a suffix in the carbon footprint.
     */

    fun updateRespectiveDataType(entity: DataPointIdAndDataPointTypeEntity) {
        if (entity.dataPointType == "extendedCurrencyTotalRevenue" ||
            entity.dataPointType == "extendedCurrencyEnterpriseValue" ||
            entity.dataPointType == "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
        ) {
            entity.dataPointType = renameMap[entity.dataPointType]
                ?: return
        }
    }
}
