package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.MigrationHelper
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

/**
 * This migration script updates the old version eutaxonomy for non financials datasets to the new version
 * and the new version is integrated into the old datatype
 */
class V8__MigratePercentages : BaseJavaMigration() {

    override fun migrate(context: Context?) {
        val migrationScriptMapping = mapOf(
            "eutaxonomy-financials" to this::migrateEuTaxonomyFinancials,
            "eutaxonomy-non-financials" to this::migrateEuTaxonomyNonFinancials,
            "lksg" to this::migrateLksg,
        )
        migrationScriptMapping.forEach {
            migrateCompanyAssociatedDataOfDatatype(
                context,
                it.key,
                it.value,
            )
        }
    }

    private fun migrateDataset(dataTableEntity: DataTableEntity, migrationScript: (JSONObject) -> Unit) {
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        migrationScript(dataObject)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    private fun migrateFinancialShare(migrationHelper: MigrationHelper, financialShareHolder: JSONObject, financialShareName: String) {
        val financialShareObject = (financialShareHolder.getOrJavaNull(financialShareName) ?: return) as JSONObject
        migrationHelper.migrateValue(financialShareObject, "relativeShareInPercent", ::transformToPercentage)
    }

    private fun transformToPercentage(decimal: BigDecimal): BigDecimal {
        return decimal * BigDecimal(100)
    }

    /**
     * Migrates a EU taxonomy for financials dataset
     */
    fun migrateEuTaxonomyFinancials(dataTableEntity: DataTableEntity) {
        migrateDataset(dataTableEntity) { dataObject ->
            val migrationHelper = MigrationHelper()
            val financialServiceTypesWithPercentageDataPointsOnly = listOf(
                "creditInstitutionKpis", "investmentFirmKpis", "insuranceKpis",
            )
            financialServiceTypesWithPercentageDataPointsOnly.forEach { financialServiceType ->
                val financialServiceKpis = (dataObject.getOrJavaNull(financialServiceType) ?: return@forEach) as JSONObject
                financialServiceKpis.keys().forEach { kpiKey ->
                    migrationHelper.migrateDataPointValueFromToAndQueueForRemoval(
                        financialServiceKpis,
                        kpiKey,
                        "${kpiKey}InPercent",
                        ::transformToPercentage,
                    )
                }
                migrationHelper.removeQueuedFields()
            }
            val eligibilityKpis = (dataObject.getOrJavaNull("eligibilityKpis") ?: return@migrateDataset) as JSONObject
            eligibilityKpis.keys().forEach { key ->
                val financialServiceKpis = (eligibilityKpis.getOrJavaNull(key) ?: return@forEach) as JSONObject
                financialServiceKpis.keys().asSequence().toList().forEach { kpiKey ->
                    migrationHelper.migrateDataPointValueFromToAndQueueForRemoval(
                        financialServiceKpis,
                        kpiKey,
                        "${kpiKey}InPercent",
                        ::transformToPercentage,
                    )
                }
                migrationHelper.removeQueuedFields()
            }
        }
    }

    /**
     * Migrates a EU taxonomy for non-financials dataset
     */
    fun migrateEuTaxonomyNonFinancials(dataTableEntity: DataTableEntity) {
        migrateDataset(dataTableEntity) { dataObject ->
            val migrationHelper = MigrationHelper()
            listOf("revenue", "capex", "opex").forEach { cashFlowType ->
                val cashFlowObject = (dataObject.getOrJavaNull(cashFlowType) ?: return@forEach) as JSONObject
                val financialShareFields = listOf("nonEligibleShare", "eligibleShare", "nonAlignedShare", "alignedShare")
                val percentageFields = listOf(
                    "substantialContributionToClimateChangeMitigationInPercent",
                    "substantialContributionToClimateChangeAdaptionInPercent",
                    "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
                    "substantialContributionToTransitionToACircularEconomyInPercent",
                    "substantialContributionToPollutionPreventionAndControlInPercent",
                    "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
                    "enablingShareInPercent",
                    "transitionalShareInPercent",
                )
                financialShareFields.forEach {
                    migrateFinancialShare(migrationHelper, cashFlowObject, it)
                }
                percentageFields.forEach {
                    migrationHelper.migrateValue(cashFlowObject, it, ::transformToPercentage)
                }
                cashFlowObject.getOrJavaNull("nonAlignedActivities")?.also {
                    migrateNonAlignedActivities(migrationHelper, it as JSONArray)
                }
                cashFlowObject.getOrJavaNull("alignedActivities")?.also {
                    migrateAlignedActivities(migrationHelper, it as JSONArray)
                }
            }
        }
    }

    private fun migrateNonAlignedActivities(migrationHelper: MigrationHelper, activities: JSONArray) {
        activities.forEach { activity ->
            migrateFinancialShare(migrationHelper, activity as JSONObject, "share")
        }
    }

    private fun migrateAlignedActivities(migrationHelper: MigrationHelper, activities: JSONArray) {
        val percentageFields = listOf(
            "substantialContributionToClimateChangeMitigationInPercent",
            "substantialContributionToClimateChangeAdaptionInPercent",
            "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
            "substantialContributionToTransitionToACircularEconomyInPercent",
            "substantialContributionToPollutionPreventionAndControlInPercent",
            "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
        )
        activities.forEach { activity ->
            migrateFinancialShare(migrationHelper, activity as JSONObject, "share")
            percentageFields.forEach { fieldName ->
                migrationHelper.migrateValue(activity, fieldName, ::transformToPercentage)
            }
        }
    }

    /**
     * Migrates an LkSG dataset
     */
    fun migrateLksg(dataTableEntity: DataTableEntity) {
        migrateDataset(dataTableEntity) { dataObject ->
            val migrationHelper = MigrationHelper()
            ((dataObject.getOrJavaNull("social") as JSONObject?)?.getOrJsonNull("disregardForFreedomOfAssociation") as JSONObject?)?.also {
                migrationHelper.migrateValueFromToAndQueueForRemoval(
                    it,
                    "employeeRepresentation",
                    "employeeRepresentationInPercent",
                    ::transformToPercentage,
                )
            }
            (
                (
                    (dataObject.getOrJavaNull("general") as JSONObject?)
                        ?.getOrJavaNull("productionSpecificOwnOperations") as JSONObject?
                    )
                    ?.getOrJavaNull("productsServicesCategoriesPurchased") as JSONObject?
                )
                ?.also { procurementCategories ->
                    (procurementCategories).keys().forEach { procurementCategoryKey ->
                        migrationHelper.migrateValueFromToAndQueueForRemoval(
                            (procurementCategories.getOrJavaNull(procurementCategoryKey) ?: return@forEach) as JSONObject,
                            "percentageOfTotalProcurement",
                            "shareOfTotalProcurementInPercent",
                            ::transformToPercentage,
                        )
                    }
                }
            migrationHelper.removeQueuedFields()
        }
    }
}
