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
            "sfdr" to this::migrateSfdr,
            "sme" to this::migrateSme,
            "p2p" to this::migrateP2p,
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

    private fun migratePercentageDataPoint(dataPointHolder: JSONObject, dataPointName: String) {
        val dataPointObject = (dataPointHolder.getOrJavaNull(dataPointName) ?: return) as JSONObject
        migratePercentageValue(dataPointObject, "value")
    }

    private fun migrateFinancialShare(financialShareHolder: JSONObject, financialShareName: String) {
        val financialShareObject = (financialShareHolder.getOrJavaNull(financialShareName) ?: return) as JSONObject
        migratePercentageValue(financialShareObject, "relativeShareInPercent")
    }

    private fun migratePercentageValue(kpiHolder: JSONObject, kpiName: String) {
        migratePercentageValueFromTo(kpiHolder, kpiName, kpiName)
    }

    private fun migratePercentageValueAppendingSuffix(kpiHolder: JSONObject, kpiName: String) {
        migratePercentageValueFromTo(kpiHolder, kpiName, "${kpiName}InPercent")
    }

    private fun migratePercentageValueFromTo(kpiHolder: JSONObject, oldKpiName: String, newKpiName: String = oldKpiName) {
        kpiHolder.put(newKpiName, transformToPercentage((kpiHolder.getOrJavaNull(oldKpiName) ?: return) as BigDecimal))
    }

    private fun transformToPercentage(decimal: BigDecimal): BigDecimal {
        return decimal * BigDecimal(100)
    }

    /**
     * Migrates a EU taxonomy for financials dataset
     */
    fun migrateEuTaxonomyFinancials(dataTableEntity: DataTableEntity) {
        // TODO manual renamings in backend
        migrateDataset(dataTableEntity) { dataObject ->
            val financialServiceTypesWithPercentageDataPointsOnly = listOf(
                "creditInstitutionKpis", "investmentFirmKpis", "insuranceKpis",
            )
            financialServiceTypesWithPercentageDataPointsOnly.forEach { financialServiceType ->
                val financialServiceKpis = (dataObject.getOrJavaNull(financialServiceType) ?: return@forEach) as JSONObject
                val kpiKeys = financialServiceKpis.keys()
                kpiKeys.forEach { kpiKey ->
                    // TODO renaming
                    migratePercentageDataPoint(financialServiceKpis, kpiKey)
                }
            }
            val eligibilityKpis = (dataObject.getOrJavaNull("eligibilityKpis") ?: return@migrateDataset) as JSONObject
            val financialServiceTypes = eligibilityKpis.keys()
            financialServiceTypes.forEach { key ->
                val financialServiceKpis = (eligibilityKpis.getOrJavaNull(key) ?: return@forEach) as JSONObject
                // TODO renaming
                val financialServiceKpiKeys = financialServiceKpis.keys()
                financialServiceKpiKeys.forEach { kpiKey ->
                    migratePercentageDataPoint(financialServiceKpis, kpiKey)
                }
            }
        }
    }

    /**
     * Migrates a EU taxonomy for non-financials dataset
     */
    fun migrateEuTaxonomyNonFinancials(dataTableEntity: DataTableEntity) {
        // TODO check for renamings for non financials
        migrateDataset(dataTableEntity) { dataObject ->
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
                    migrateFinancialShare(cashFlowObject, it)
                }
                percentageFields.forEach {
                    migratePercentageValue(cashFlowObject, it)
                }
                cashFlowObject.getOrJavaNull("nonAlignedActivities")?.also {
                    migrateNonAlignedActivities(it as JSONArray)
                }
                cashFlowObject.getOrJavaNull("alignedActivities")?.also {
                    migrateAlignedActivities(it as JSONArray)
                }
            }
        }
    }

    private fun migrateNonAlignedActivities(activities: JSONArray) {
        activities.forEach { activity ->
            migrateFinancialShare(activity as JSONObject, "share")
        }
    }

    private fun migrateAlignedActivities(activities: JSONArray) {
        val percentageFields = listOf(
            "substantialContributionToClimateChangeMitigationInPercent",
            "substantialContributionToClimateChangeAdaptionInPercent",
            "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
            "substantialContributionToTransitionToACircularEconomyInPercent",
            "substantialContributionToPollutionPreventionAndControlInPercent",
            "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent",
        )
        activities.forEach { activity ->
            migrateFinancialShare(activity as JSONObject, "share")
            percentageFields.forEach { fieldName ->
                migratePercentageValue(activity, fieldName)
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
                    ::transformToPercentage
                )
            }
            (((dataObject.getOrJavaNull("general") as JSONObject?)
                ?.getOrJavaNull("productionSpecificOwnOperations") as JSONObject?)
                ?.getOrJavaNull("productsServicesCategoriesPurchased") as JSONObject?)
                ?.also { procurementCategories ->
                        (procurementCategories).keys().forEach { procurementCategoryKey ->
                            migrationHelper.migrateValueFromToAndQueueForRemoval(
                                (procurementCategories.getOrJavaNull(procurementCategoryKey) ?: return@forEach) as JSONObject,
                                "percentageOfTotalProcurement",
                                "shareOfTotalProcurementInPercent",
                                ::transformToPercentage
                            )
                       }
                }
            migrationHelper.removeQueuedFields()
        }
    }

    /**
     * Migrates an SFDR dataset
     */
    fun migrateSfdr(dataTableEntity: DataTableEntity) {
        migrateDataset(dataTableEntity) { dataObject ->
            // TODO only change names if neccessary
        }
    }

    /**
     * Migrates an SME dataset
     */
    fun migrateSme(dataTableEntity: DataTableEntity) {
        migrateDataset(dataTableEntity) { dataObject ->
            val production = (dataObject.getOrJavaNull("production") ?: return@migrateDataset) as JSONObject
            val fieldPaths = listOf(
                Pair("sites", "listOfProductionSites"),
                Pair("products", "listOfProducts"),
            )
            fieldPaths.forEach { fieldPath ->
                ((production.getOrJavaNull(fieldPath.first) as JSONObject?)
                    ?.getOrJavaNull(fieldPath.second) as JSONArray?)
                    ?.forEach {
                        migratePercentageValue(
                            it as JSONObject,
                            "percentageOfTotalRevenue", // TODO rename
                        )
                    }
            }
        }
    }

    /**
     * Migrates a Pathways to Paris dataset
     */
    fun migrateP2p(dataTableEntity: DataTableEntity) {
        val percentageFieldNames = setOf(
            "parisCompatibilityInExecutiveRemuneration",
            "parisCompatibilityInAverageRemuneration",
            "shareOfEmployeesTrainedOnParisCompatibility",
            "reductionOfRelativeEmissions",
            "relativeEmissions",
            "capexShareInGhgIntensivePlants",
            "capexShareInNetZeroSolutions",
            "researchAndDevelopmentExpenditureForNetZeroSolutions",
            "energyMix",
            "ccsTechnologyAdoption",
            "electrification",
            "useOfRenewableFeedstocks",
            "energyMix",
            "driveMix",
            "materialUseManagement",
            "useOfSecondaryMaterials",
            "energyMix",
            "electrification",
            "useOfRenewableFeedstocks",
            "useOfBioplastics",
            "useOfCo2FromCarbonCaptureAndReUseTechnologies",
            "materialRecycling",
            "chemicalRecycling",
            "buildingSpecificReburbishmentRoadmap",
            "zeroEmissionBuildingShare",
            "renewableHeating",
            "blastFurnacePhaseOut",
            "fuelMix",
            "lowCarbonSteelScaleUp",
            "shareOfRenewableElectricity",
            "compostedFermentedManure",
            "emissionProofFertiliserStorage",
            "storageCapacityExpansion",
            "mortalityRate",
            "ownFeedPercentage",
            "climateFriendlyProteinProduction",
            "greenFodderPercentage",
            "renewableElectricityPercentage",
            "renewableHeatingPercentage",
            "electricGasPoweredMachineryVehiclePercentage",
            "energyMix",
            "fuelMix",
            "thermalEnergyEfficiency",
            "compositionOfThermalInput",
            "electrificationOfProcessHeat",
            "preCalcinedClayUsage",
        )
        migrateDataset(dataTableEntity) { dataObject ->
            val migrationHelper = MigrationHelper()
            dataObject.keys().forEach { categoryName ->
                val category = (dataObject.getOrJavaNull(categoryName) ?: return@forEach) as JSONObject
                category.keys().forEach { subcategoryName ->
                    val subcategory = (category.getOrJavaNull(subcategoryName) ?: return@forEach) as JSONObject
                    subcategory.keys().forEach { fieldName ->
                        if(fieldName in percentageFieldNames) {
                            migrationHelper.migrateValueFromToAndQueueForRemoval(
                                subcategory,
                                fieldName,
                                "${fieldName.removeSuffix("Percentage")}InPercent",
                                ::transformToPercentage
                            )
                        }
                    }
                    migrationHelper.removeQueuedFields()
                }
            }
        }
    }
}
