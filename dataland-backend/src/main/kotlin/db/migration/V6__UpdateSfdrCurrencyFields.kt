package db.migration

import db.migration.utils.DataPointIdAndDataPointTypeEntity
import db.migration.utils.migrateBackendTable
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates the meta information of currency related Sfdr data points, and corrects a suffix in the
 * carbon footprint.
 */
@Suppress("ClassName")
class V6__UpdateSfdrCurrencyFields : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val metaResultSet = connection.metaData.getTables(null, null, "data_point_meta_information", null)
        val uuidResultSet = connection.metaData.getTables(null, null, "data_point_uuid_map", null)

        if (metaResultSet.next() && uuidResultSet.next()) {
            migrateMetaInformationTable(context, "extendedCurrencyTotalRevenue")
            migrateMetaInformationTable(context, "extendedCurrencyRevenue")
            migrateMetaInformationTable(context, "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue")
            migrateUuidInformationTable(context, "extendedCurrencyTotalRevenue")
            migrateMetaInformationTable(context, "extendedCurrencyRevenue")
            migrateMetaInformationTable(context, "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue")
        }
    }

    fun migrateMetaInformationTable(
        context: Context?,
        dataPointType: String,
    ) {
        migrateBackendTable(
            context,
            dataPointType,
            "data_point_meta_information",
            "data_point_type",
        ) { this.updateRespectiveDataType(it) }
    }

    fun migrateUuidInformationTable(
        context: Context?,
        dataPointType: String,
    ) {
        migrateBackendTable(
            context,
            dataPointType,
            "data_point_uuid_map",
            "data_point_type",
        ) { this.updateRespectiveDataType(it) }
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
