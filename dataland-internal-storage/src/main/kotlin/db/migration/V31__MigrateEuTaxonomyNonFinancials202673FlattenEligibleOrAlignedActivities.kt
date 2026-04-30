package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script removes the extra nesting layer from the eligibleOrAlignedActivities field
 * in eutaxonomy-non-financials-2026-73 datasets.
 *
 * Before: revenue.eligibleOrAlignedActivities.eligibleOrAlignedActivities = { value: [...], ... }
 * After:  revenue.eligibleOrAlignedActivities = { value: [...], ... }
 */
@Suppress("ClassName")
class V31__MigrateEuTaxonomyNonFinancials202673FlattenEligibleOrAlignedActivities : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials-2026-73",
            this::flattenEligibleOrAlignedActivities,
        )
    }

    /**
     * Unwraps the double-nested eligibleOrAlignedActivities field for all three KPI types.
     */
    fun flattenEligibleOrAlignedActivities(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject
        listOf("revenue", "capex", "opex").forEach { kpi ->
            val kpiObject = dataset.getOrJavaNull(kpi) as? JSONObject ?: return@forEach
            flattenActivitiesInKpi(kpiObject)
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    private fun flattenActivitiesInKpi(kpiObject: JSONObject) {
        val wrapper = kpiObject.getOrJavaNull("eligibleOrAlignedActivities") as? JSONObject ?: return
        val inner = wrapper.getOrJavaNull("eligibleOrAlignedActivities") as? JSONObject ?: return
        kpiObject.put("eligibleOrAlignedActivities", inner)
    }
}
