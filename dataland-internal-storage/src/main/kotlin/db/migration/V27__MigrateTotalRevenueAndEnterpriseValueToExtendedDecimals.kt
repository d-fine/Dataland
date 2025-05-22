package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.migrateCompanyAssociatedDatapointOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates the EU Taxonomy for non-financials data model switching
 * from a field called value for percentages only to a structure holding the absolute value of a cash flow type as well
 */
@Suppress("ClassName")
class V27__MigrateTotalRevenueAndEnterpriseValueToExtendedDecimals : BaseJavaMigration() {
    /**
     * Migrates an old eu taxonomy non financials dataset to the new format
     */
    fun updateDatapointTypesAndRemoveCurrencyIfApplicable(dataPointTableEntity: DataPointTableEntity) {
        if (dataPointTableEntity.dataPointType == "extendedCurrencyTotalRevenue" ||
            dataPointTableEntity.dataPointType == "extendedCurrencyEnterpriseValue"
        ) {
            dataPointTableEntity.companyAssociatedData.remove("currency")
        }
        val renameMap =
            mapOf(
                "extendedCurrencyTotalRevenue" to "extendedDecimalTotalRevenueInEUR",
                "extendedCurrencyEnterpriseValue" to "extendedDecimalEnterpriseValueInEUR",
                "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
                    to "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
            )
        dataPointTableEntity.dataPointType = renameMap[dataPointTableEntity.dataPointType]
            ?: return
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDatapointOfDatatype(
            context,
            "extendedCurrencyTotalRevenue",
        ) { this.updateDatapointTypesAndRemoveCurrencyIfApplicable(it) }

        migrateCompanyAssociatedDatapointOfDatatype(
            context,
            "extendedCurrencyEnterpriseValue",
            this::updateDatapointTypesAndRemoveCurrencyIfApplicable,
        )

        migrateCompanyAssociatedDatapointOfDatatype(
            context,
            "extendedCurrencyCarbonFootprintInTonnesPerMillionEURRevenue",
            this::updateDatapointTypesAndRemoveCurrencyIfApplicable,
        )
    }
}
