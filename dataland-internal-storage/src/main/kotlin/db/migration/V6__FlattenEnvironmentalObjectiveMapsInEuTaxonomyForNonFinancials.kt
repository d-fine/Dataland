package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * This migration script updates the old version eutaxonomy for non financials datasets to the new version
 * and the new version is integrated into the old datatype
 */
class V6__FlattenEnvironmentalObjectiveMapsInEuTaxonomyForNonFinancials : BaseJavaMigration() {
    private enum class CriteriaType(val prefix: String) {
        SubstantialContribution("substantialContribution"),
        Dnsh("dnsh"),
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials",
            this::flattenEnvironmentalObjectiveMaps,
        )
    }

    /**
     * Migrates an old eu taxonomy non financials dataset to the new format
     */
    fun flattenEnvironmentalObjectiveMaps(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        listOf("revenue", "capex", "opex").forEach {
            migrateCashFlow(dataset.getOrJavaNull(it) as JSONObject? ?: return@forEach)
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    private fun migrateCashFlow(cashFlow: JSONObject) {
        migrateFieldNames(cashFlow)
        migrateMapForCriteria(cashFlow, CriteriaType.SubstantialContribution)
        (cashFlow.getOrJavaNull("alignedActivities") as JSONArray? ?: return).forEach {
            migrateMapForCriteria(it as JSONObject, CriteriaType.SubstantialContribution)
            migrateMapForCriteria(it, CriteriaType.Dnsh)
        }
    }

    private fun migrateMapForCriteria(baseObject: JSONObject, criteriaType: CriteriaType) {
        val fieldRenamingHelper = mapOf(
            "ClimateMitigation" to "ClimateChangeMitigation",
            "ClimateAdaptation" to "ClimateChangeAdaption",
            "Water" to "SustainableWaterUse",
            "CircularEconomy" to "CircularEconomy",
            "PollutionPrevention" to "PollutionPreventionAndControl",
            "Biodiversity" to "Biodiversity",
        )
        val criteriaObject = baseObject.opt("${criteriaType.prefix}Criteria") ?: return
        if (criteriaObject is JSONObject) {
            fieldRenamingHelper.keys.forEach { oldFieldName ->
                if (criteriaObject.has(oldFieldName)) {
                    baseObject.put(
                        "${criteriaType.prefix}To${fieldRenamingHelper[oldFieldName]}",
                        criteriaObject.getOrJsonNull(oldFieldName),
                    )
                }
            }
        }
        baseObject.remove("${criteriaType.prefix}Criteria")
    }

    private fun migrateFieldNames(baseObject: JSONObject) {
        val fieldsToRename = listOf(
            "nonEligibleShare", "eligibleShare", "nonAlignedShare", "alignedShare", "enablingShare", "transitionShare"
        )
        val fieldNamesWithoutTotalPrefix = fieldsToRename.associateBy({"total${it.replaceFirstChar(Char::titlecase)}"}, {it})
        println(fieldNamesWithoutTotalPrefix)
        fieldNamesWithoutTotalPrefix.keys.forEach { oldFieldName ->
            if (baseObject.has(oldFieldName)) {
                baseObject.put(
                    fieldNamesWithoutTotalPrefix.getValue(oldFieldName), baseObject.getOrJsonNull(oldFieldName)
                )
                baseObject.remove(oldFieldName)
            }
        }
    }
}
