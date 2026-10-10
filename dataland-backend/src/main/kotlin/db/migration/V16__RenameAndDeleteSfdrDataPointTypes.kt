package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Renames and removes SFDR data point types in the backend's data point metadata tables to match the reworked
 * SFDR data model (7 renames, 11 deletions). The actual data point values live in dataland-internal-storage; this
 * migration only keeps the backend's composition metadata (data_point_meta_information, data_point_uuid_map) in
 * sync with the renamed/deleted data point types.
 */
@Suppress("ClassName")
class V16__RenameAndDeleteSfdrDataPointTypes : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        val renameMap =
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
        val connection = context!!.connection
        val metaTable = "data_point_meta_information"
        val uuidTable = "data_point_uuid_map"

        if (connection.metaData.getTables(null, null, metaTable, null).next()) {
            renameMap.forEach { (sourceType, targetType) ->
                renameDataPointType(context, metaTable, "data_point_type", sourceType, targetType)
            }
            deletedDataPointTypes.forEach { dataPointType ->
                deleteDataPointType(context, metaTable, "data_point_type", dataPointType)
            }
        }

        if (connection.metaData.getTables(null, null, uuidTable, null).next()) {
            renameMap.forEach { (sourceType, targetType) ->
                renameDataPointType(context, uuidTable, "data_point_identifier", sourceType, targetType)
            }
            deletedDataPointTypes.forEach { dataPointType ->
                deleteDataPointType(context, uuidTable, "data_point_identifier", dataPointType)
            }
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
        val statement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET $columnName = ? WHERE $columnName = ?",
            )
        statement.setString(1, targetType)
        statement.setString(2, sourceType)
        val updatedRows = statement.executeUpdate()
        statement.close()

        logger.info("Updated $updatedRows rows in $tableName from \"$sourceType\" to \"$targetType\"")
    }

    /**
     * Deletes all rows matching a data point type in the selected table and column.
     */
    fun deleteDataPointType(
        context: Context,
        tableName: String,
        columnName: String,
        dataPointType: String,
    ) {
        val statement =
            context.connection.prepareStatement(
                "DELETE FROM $tableName WHERE $columnName = ?",
            )
        statement.setString(1, dataPointType)
        val deletedRows = statement.executeUpdate()
        statement.close()

        logger.info("Deleted $deletedRows rows in $tableName with \"$dataPointType\"")
    }
}
