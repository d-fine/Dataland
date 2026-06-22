package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Renames the EU Taxonomy Financials 2026/73 GAR assets data point types to their turnover-based names.
 */
@Suppress("ClassName")
class V15__RenameGarAssetsDataPointTypes : BaseJavaMigration() {
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
    }

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val metaTable = "data_point_meta_information"
        val uuidTable = "data_point_uuid_map"

        if (connection.metaData.getTables(null, null, metaTable, null).next()) {
            renameMap.forEach { (sourceType, targetType) ->
                renameDataPointType(context, metaTable, "data_point_type", sourceType, targetType)
            }
        }

        if (connection.metaData.getTables(null, null, uuidTable, null).next()) {
            renameMap.forEach { (sourceType, targetType) ->
                renameDataPointType(context, uuidTable, "data_point_identifier", sourceType, targetType)
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
}
