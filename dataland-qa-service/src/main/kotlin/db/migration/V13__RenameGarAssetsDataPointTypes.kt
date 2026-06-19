package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Renames the EU Taxonomy Financials 2026/73 GAR assets data point types in QA tables.
 */
@Suppress("ClassName")
class V13__RenameGarAssetsDataPointTypes : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val OLD_ASSETS_PREFIX =
            "extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatio"
        private const val NEW_ASSETS_PREFIX =
            "extendedCurrencyCreditInstitutionTurnoverBasedAssetsForCalculationOfGreenAssetRatioTurnoverBased"
        private val dataPointTypeSuffixes =
            listOf(
                "TotalAmountOfNonAssessedExposures",
                "TotalAmountOfNonAssessedExposuresOfWhichExposuresFinancingCounterpartiesReportingNoTaxonomyEligibleActivities",
                "TotalAmountOfNonAssessedExposuresOfWhichFinancingNonMaterialActivitiesOfCounterparties",
                "TotalAmountOfNonAssessedExposuresOfWhichNotAssessedConsideredNonMaterialByTheCreditInstitution",
            )

        val renameMap =
            dataPointTypeSuffixes.associate {
                "$OLD_ASSETS_PREFIX$it" to "$NEW_ASSETS_PREFIX$it"
            }

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
}
