package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the EU Taxonomy for non-financials data model switching
 * from a field called value for percentages only to a structure holding the absolute value of a cash flow type as well
 */
@Suppress("ClassName")
class V2__MigrateEuTaxonomyNonFinancialsWithAbsoluteValues : BaseJavaMigration() {
    private val cashFlowTypes = listOf("capex", "opex", "revenue")
    private val fieldsToMigrate = mapOf("alignedPercentage" to "alignedData", "eligiblePercentage" to "eligibleData")

    private fun migrateFieldForCashFlow(
        fieldToMigrate: String,
        cashFlow: JSONObject,
    ) {
        val dataToMigrate = cashFlow.opt(fieldToMigrate) ?: return
        if (dataToMigrate != JSONObject.NULL && dataToMigrate is JSONObject) {
            dataToMigrate.put("valueAsPercentage", dataToMigrate.getOrJsonNull("value"))
            dataToMigrate.remove("value")
        }
        cashFlow.put(fieldsToMigrate.getValue(fieldToMigrate), dataToMigrate)
        cashFlow.remove(fieldToMigrate)
    }

    /**
     * Migrates an old eu taxonomy non financials dataset to the new format
     */
    fun migrateEuTaxonomyNonFinancialsData(dataTableEntity: DataTableEntity) {
        val dataset = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        cashFlowTypes.forEach { cashflowType ->
            val cashFlow = (dataset.getOrJavaNull(cashflowType) ?: return@forEach) as JSONObject
            fieldsToMigrate.keys.forEach { fieldToMigrate ->
                migrateFieldForCashFlow(fieldToMigrate, cashFlow)
            }
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials", this::migrateEuTaxonomyNonFinancialsData,
        )
    }
}
