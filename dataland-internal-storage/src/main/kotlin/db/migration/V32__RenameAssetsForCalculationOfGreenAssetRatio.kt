package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script renames data point types
 */
@Suppress("ClassName", "MaxLineLength")
class V32__RenameAssetsForCalculationOfGreenAssetRatio : BaseJavaMigration() {
    companion object {
        private const val OLD_PREFIX =
            "extendedCurrencyCreditInstitutionAssetsForCalculationOfGreenAssetRatio"
        private const val NEW_PREFIX =
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
                "$OLD_PREFIX$it" to "$NEW_PREFIX$it"
            }
    }

    /**
     * Updates the data point type based on the rename map
     */
    fun updateDataTableEntity(entity: DataPointTableEntity) {
        entity.dataPointType = renameMap.getOrDefault(entity.dataPointType, entity.dataPointType)
    }

    override fun migrate(context: Context?) {
        if (context!!
                .connection.metaData
                .getTables(
                    null, null, "data_point_items", null,
                ).next()
        ) {
            renameMap.keys.forEach {
                migrateDataPointTableEntities(context, it, ::updateDataTableEntity)
            }
        }
    }
}
