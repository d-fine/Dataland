package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates currency-related Sfdr data points to the extendedDecimal data point type.
 * It also changes the suffix of the suffix of the CarbonFootprintInTonnesPerMillionEURRevenue date point type.
 */
@Suppress("ClassName")
class V27__UpdateSfdrCurrencyFields : BaseJavaMigration() {
    val renameMap =
        mapOf(
            "extendedCurrencyTotalRevenue" to "extendedDecimalTotalRevenueInEUR",
            "extendedCurrencyEnterpriseValue" to "extendedDecimalEnterpriseValueInEUR",
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
                to "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
        )

    /**
     * Updates currency-related Sfdr data points to the extendedDecimal data point type.
     */
    fun updateCurrencyFieldsToDecimals(dataPointTableEntity: DataPointTableEntity) {
        if (dataPointTableEntity.dataPointType == "extendedCurrencyTotalRevenue" ||
            dataPointTableEntity.dataPointType == "extendedCurrencyEnterpriseValue"
        ) {
            dataPointTableEntity.dataPoint.remove("currency")
            dataPointTableEntity.dataPointType = renameMap[dataPointTableEntity.dataPointType]
                ?: return
        }
    }

    /**
     * Updates the suffix of the suffix of the CarbonFootprintInTonnesPerMillionEURRevenue date point type.
     */
    fun updateCarbonFootprint(dataPointTableEntity: DataPointTableEntity) {
        if (dataPointTableEntity.dataPointType == "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue") {
            dataPointTableEntity.dataPointType = renameMap[dataPointTableEntity.dataPointType]
                ?: return
        }
    }

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val resultSet = connection.metaData.getTables(null, null, "data_point_meta_information", null)
        if (resultSet.next()) {
            migrateDataPointTableEntities(
                context,
                "extendedCurrencyTotalRevenue",
            ) { this.updateCurrencyFieldsToDecimals(it) }

            migrateDataPointTableEntities(
                context,
                "extendedCurrencyEnterpriseValue",
                this::updateCurrencyFieldsToDecimals,
            )

            migrateDataPointTableEntities(
                context,
                "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
                this::updateCarbonFootprint,
            )
        }
    }
}
