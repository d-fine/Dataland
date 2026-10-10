package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates the SFDR data points to match the reworked SFDR data model:
 * - renames 7 data point types
 * - deletes 11 obsolete data point types
 * SFDR is an assembled framework, so no dataset blobs have to be touched.
 */
@Suppress("ClassName")
class V34__RenameDeleteAndAddSfdrFields : BaseJavaMigration() {
    companion object {
        val dataPointTypeRenameMap =
            mapOf(
                "extendedDecimalWaterConsumptionInCubicMeters" to "extendedDecimalWaterWithdrawalInCubicMeters",
                "extendedDecimalRelativeWaterUsageInCubicMetersPerMillionEURRevenue" to
                    "extendedDecimalWaterWithdrawalIntensityInCubicMetersPerMillionEURRevenue",
                "extendedEnumYesNoViolationOfTaxRulesAndRegulation" to
                    "extendedEnumYesNoViolationOfUngcPrinciplesAndOecdGuidelines",
                "extendedEnumYesNoReportedChildLabourIncidents" to "extendedEnumYesNoRiskOfChildLabourIncidents",
                "extendedEnumYesNoReportedForcedOrCompulsoryLabourIncidents" to
                    "extendedEnumYesNoRiskOfReportedForcedOrCompulsoryLabourIncidents",
                "extendedIntegerReportedConvictionsOfBriberyAndCorruption" to
                    "extendedIntegerNumberOfReportedConvictionsOfBriberyAndCorruption",
                "extendedEnumYesNoLandDegradationDesertificationSoilSealingExposure" to
                    "extendedEnumYesNoLandDegradationDesertificationSoilSealing",
            )

        val deletedDataPointTypes =
            listOf(
                "extendedCurrencyAverageGrossHourlyEarningsFemaleEmployees",
                "extendedCurrencyAverageGrossHourlyEarningsMaleEmployees",
                "extendedDecimalGhgIntensityScope4InTonnesPerMillionEURRevenue",
                "extendedDecimalScope4GhgEmissionsInTonnes",
                "extendedEnumYesNoHighlyBiodiverseGrasslandExposure",
                "extendedEnumYesNoIso14001Certificate",
                "extendedEnumYesNoSecuritiesNotCertifiedAsGreen",
                "extendedEnumYesNoTechnologiesExpertiseTransferPolicy",
                "extendedEnumYesNoTransparencyDisclosurePolicy",
                "plainSfdrHighImpactClimateSectorsApplicableHighImpactClimateSectors",
                "plainDateSfdrDataDate",
            )
    }

    override fun migrate(context: Context?) {
        if (dataPointItemsTableExists(context)) {
            renameDataPointTypes(context)
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

    /**
     * Renames the data point types of the renamed KPIs in the data_point_items table.
     */
    private fun renameDataPointTypes(context: Context?) {
        context!!
            .connection
            .prepareStatement("UPDATE data_point_items SET data_point_type = ? WHERE data_point_type = ?")
            .use { statement ->
                dataPointTypeRenameMap.forEach { (sourceType, targetType) ->
                    statement.setString(1, targetType)
                    statement.setString(2, sourceType)
                    statement.executeUpdate()
                }
            }
    }

    /**
     * Deletes all data_point_items rows of the obsolete data point types.
     */
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
