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
class V5__MigrateToNewEuTaxonomyForNonFinancials : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateOldData(context)
        migrateNewData(context)
    }

    /**
     * Migrates an old eu taxonomy non financials dataset to the new format
     */
    fun migrateEuTaxonomyData(dataTableEntity: DataTableEntity) {
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        migrateGeneralFields(dataObject)
        listOf("revenue", "capex", "opex").forEach { cashFlowType ->
            val cashFlowObject = dataObject.getOrJavaNull(cashFlowType) ?: return@forEach
            migrateOldCashFlowDetails(cashFlowObject as JSONObject)
        }
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    private fun migrateOldData(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(context, "eutaxonomy-non-financials", this::migrateEuTaxonomyData)
    }

    private fun migrateGeneralFields(dataObject: JSONObject) {
        val generalObject = JSONObject()
        val keysToMove =
            listOf(
                "fiscalYearDeviation",
                "fiscalYearEnd",
                "scopeOfEntities",
                "nfrdMandatory",
                "euTaxonomyActivityLevelReporting",
                "assurance",
                "numberOfEmployees",
                "referencedReports",
            )
        keysToMove.forEach {
            generalObject.put(it, dataObject.getOrJsonNull(it))
            dataObject.remove(it)
        }
        dataObject.put("general", generalObject)
    }

    private fun migrateTotalAmount(cashFlowDetails: JSONObject) {
        val totalAmountObject = cashFlowDetails.getOrJavaNull("totalAmount")
        if (totalAmountObject != null) {
            totalAmountObject as JSONObject

            val oldTotalAmountValue = totalAmountObject.getOrJavaNull("value")
            totalAmountObject.put(
                "value",
                if (oldTotalAmountValue == null) {
                    JSONObject.NULL
                } else {
                    val newTotalAmountValue = JSONObject()
                    newTotalAmountValue.put("amount", oldTotalAmountValue)
                    newTotalAmountValue.put("currency", JSONObject.NULL)
                    newTotalAmountValue
                },
            )
            if (!isDataPointProvidingSourceInfo(totalAmountObject)) {
                setAlternativeSourceInfoIfPossible(cashFlowDetails)
            }
        } else if (!setAlternativeSourceInfoIfPossible(cashFlowDetails)) {
            cashFlowDetails.put("totalAmount", JSONObject.NULL)
        } else {
            // No action required
        }
    }

    private fun migrateOldCashFlowDetails(cashFlowDetails: JSONObject) {
        migrateTotalAmount(cashFlowDetails)
        migrateDataPointToFinancialShare(cashFlowDetails, "eligibleData", "totalEligibleShare")
        migrateDataPointToFinancialShare(cashFlowDetails, "alignedData", "totalAlignedShare")
    }

    private fun setAlternativeSourceInfoIfPossible(cashFlowDetails: JSONObject): Boolean {
        listOf("eligibleData", "alignedData").forEach {
            cashFlowDetails.getOrJavaNull(it)?.let { obj ->
                if (isDataPointProvidingSourceInfo(obj as JSONObject)) {
                    applyAlternativeSourceInfo(cashFlowDetails, obj)
                    return true
                }
            }
        }
        return false
    }

    private fun isDataPointProvidingSourceInfo(dataPoint: JSONObject): Boolean {
        val hasCommentOrQuality = listOf("comment", "quality").any { dataPoint.getOrJavaNull(it) != null }
        val hasPopulatedDataSource =
            dataPoint.getOrJavaNull("dataSource")?.let {
                val itObject = it as JSONObject
                itObject.keySet().any { itObject.getOrJavaNull(it) != null }
            } ?: false

        return hasCommentOrQuality || hasPopulatedDataSource
    }

    private fun applyAlternativeSourceInfo(
        cashFlowDetails: JSONObject,
        dataPoint: JSONObject,
    ) {
        val newTotalAmountObject = JSONObject()
        listOf("comment", "quality", "comment", "dataSource").forEach {
            newTotalAmountObject.put(it, dataPoint.getOrJsonNull(it))
        }
        val totalAmountValueObject =
            cashFlowDetails
                .getOrJavaNull("totalAmount")
                ?.let { (it as JSONObject).getOrJsonNull("value") } ?: JSONObject.NULL

        newTotalAmountObject.put("value", totalAmountValueObject)
        cashFlowDetails.put("totalAmount", newTotalAmountObject)
    }

    private fun migrateDataPointToFinancialShare(
        cashFlowDetails: JSONObject,
        fromKey: String,
        toKey: String,
    ) {
        val financialShareObject = JSONObject()
        cashFlowDetails.getOrJavaNull(fromKey)?.also {
            financialShareObject.put(
                "relativeShareInPercent",
                (it as JSONObject).opt("valueAsPercentage") ?: JSONObject.NULL,
            )
            val absoluteShareObject = JSONObject()
            financialShareObject.put("absoluteShare", absoluteShareObject)
            absoluteShareObject.put("amount", it.opt("valueAsAbsolute") ?: JSONObject.NULL)
            absoluteShareObject.put("currency", JSONObject.NULL)
        }
        cashFlowDetails.put(toKey, financialShareObject)
        cashFlowDetails.remove(fromKey)
    }

    private fun migrateNewData(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(context, "new-eutaxonomy-non-financials", this::migrateNewEuTaxonomyData)
    }

    /**
     * Migrates an old eu taxonomy non financials dataset to the new format
     */
    fun migrateNewEuTaxonomyData(dataTableEntity: DataTableEntity) {
        dataTableEntity.companyAssociatedData.put("dataType", "eutaxonomy-non-financials")
    }
}
