package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the old version eutaxonomy for non financials datasets to the new version
 * and the new version is integrated into the old datatype
 */
@Suppress("ClassName")
class V7__MigrateFromAmountWithCurrencyToUnit : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "eutaxonomy-non-financials",
            this::migrateEuTaxonomyAmountWithCurrencyData,
        )
    }

    /**
     * Migrate the value fields of totalAmount to the value/unit format instead of value: amount/currency
     */
    fun migrateEuTaxonomyAmountWithCurrencyData(dataTableEntity: DataTableEntity) {
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            val cashFlowObject = dataObject.getOrJavaNull(cashFlowType) ?: return@forEach
            migrateTotalAmount(cashFlowObject as JSONObject)
        }
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    private fun migrateTotalAmount(cashFlowObject: JSONObject) {
        val totalAmountObject = cashFlowObject.getOrJsonNull("totalAmount")
        if (totalAmountObject != JSONObject.NULL) {
            val oldValue = (totalAmountObject as JSONObject).getOrJsonNull("value")
            if (oldValue != JSONObject.NULL) {
                oldValue as JSONObject
                totalAmountObject.put("value", oldValue.getOrJsonNull("amount"))
                totalAmountObject.put("unit", oldValue.getOrJsonNull("currency"))
            } else {
                totalAmountObject.put("unit", JSONObject.NULL)
            }
        }
    }
}
