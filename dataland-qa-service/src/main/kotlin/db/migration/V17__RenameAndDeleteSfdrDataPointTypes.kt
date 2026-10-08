package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Renames and removes SFDR data point types in the QA service tables to match the reworked SFDR data model
 * (7 renames, 11 deletions).
 */
@Suppress("ClassName")
class V17__RenameAndDeleteSfdrDataPointTypes : BaseJavaMigration() {
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

        val tablesWithDataPointType =
            listOf(
                "data_point_qa_review",
                "data_point_qa_reports",
                "dataset_judgement_entity_data_point_judgement",
            )
    }

    override fun migrate(context: Context?) {
        tablesWithDataPointType.forEach { tableName ->
            if (context!!
                    .connection
                    .metaData
                    .getTables(null, null, tableName, null)
                    .next()
            ) {
                renameMap.forEach { (sourceType, targetType) ->
                    renameDataPointType(context, tableName, sourceType, targetType)
                }
                deletedDataPointTypes.forEach { dataPointType ->
                    deleteDataPointType(context, tableName, dataPointType)
                }
            }
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
        val statement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET data_point_type = ? WHERE data_point_type = ?",
            )
        statement.setString(1, targetType)
        statement.setString(2, sourceType)
        val updatedRows = statement.executeUpdate()
        statement.close()

        logger.info("Updated $updatedRows rows in $tableName from \"$sourceType\" to \"$targetType\"")
    }

    /**
     * Deletes all rows matching a data point type in the selected QA table.
     */
    fun deleteDataPointType(
        context: Context,
        tableName: String,
        dataPointType: String,
    ) {
        val statement =
            context.connection.prepareStatement(
                "DELETE FROM $tableName WHERE data_point_type = ?",
            )
        statement.setString(1, dataPointType)
        val deletedRows = statement.executeUpdate()
        statement.close()

        logger.info("Deleted $deletedRows rows in $tableName with \"$dataPointType\"")
    }
}
