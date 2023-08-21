package db.migration

import db.migration.utils.getCompanyAssociatedDatasetsForDataType
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import java.math.BigDecimal

/**
 * This migration script updates the old version eutaxonomy for non financials datasets to the new version
 * and the new version is integrated into the old datatype
 */
class V5__MigrateToNewEuTaxonomyForNonFinancials : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateOldData(context)
        migrateNewData(context)
    }

    private fun migrateOldData(context: Context?) {
        val companyAssociatedDatasets = getCompanyAssociatedDatasetsForDataType(context, "eutaxonomy-non-financials")
        companyAssociatedDatasets.forEach {
            val dataObject = JSONObject(it.companyAssociatedData.getString("data"))
            migrateGeneralFields(dataObject)
            listOf("revenue", "capex", "opex").forEach { cashFlowType ->
                val cashFlowObject = dataObject.getJSONObject(cashFlowType)
                migrateOldCashFlowDetails(cashFlowObject)
            }
            it.companyAssociatedData.put("data", dataObject.toString())
            context!!.connection.createStatement().execute(it.getWriteQuery())
        }

    }

    private fun migrateGeneralFields(dataObject: JSONObject) {
        val generalObject = JSONObject()
        val keysToMove = listOf(
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
            generalObject.put(it, dataObject.opt(it) ?: JSONObject.NULL)
            dataObject.remove(it)
        }
        dataObject.put("general", generalObject)
    }

    private fun migrateOldCashFlowDetails(cashFlowDetails: JSONObject) {
        val totalAmountObject = cashFlowDetails.opt("totalAmount")
        if(totalAmountObject != null && totalAmountObject != JSONObject.NULL && totalAmountObject is JSONObject) {
            val oldTotalAmountValue = totalAmountObject.opt("value")
            totalAmountObject.put("value", if(oldTotalAmountValue is BigDecimal) {
                val newTotalAmountValue = JSONObject()
                newTotalAmountValue.put("amount", oldTotalAmountValue)
                newTotalAmountValue.put("currency", JSONObject.NULL)
            } else {
                JSONObject.NULL
            })
        } else {
            cashFlowDetails.put("totalAmount", JSONObject.NULL)
        }

        migrateDataPointToFinancialShare(cashFlowDetails, "eligibleData", "totalEligibleShare")
        migrateDataPointToFinancialShare(cashFlowDetails, "alignedData", "totalAlignedShare")

        cashFlowDetails.remove("alignedData")
        val unprovidedFields = listOf(
            "totalNonEligibleShare",
            "totalNonAlignedShare",
            "nonAlignedActivities",
            "substantialContributionCriteria",
            "alignedActivities",
            "totalEnablingShare",
            "totalTransitionalShare",
        )
        unprovidedFields.forEach { cashFlowDetails.put(it, JSONObject.NULL) }
    }

    private fun migrateDataPointToFinancialShare(cashFlowDetails: JSONObject, fromKey: String, toKey: String) {
        val financialShareObject = JSONObject()
        with(cashFlowDetails.opt(fromKey) ?: JSONObject.NULL) {
            if(this == JSONObject.NULL) {
                JSONObject.NULL
            } else {
                financialShareObject.put("relativeShareInPercent", (this as JSONObject).opt("valueAsPercentage") ?: JSONObject.NULL)
                val absoluteShareObject = JSONObject()
                financialShareObject.put("absoluteShare", absoluteShareObject)
                absoluteShareObject.put("amount", this.opt("valueAsAbsolute") ?: JSONObject.NULL)
                absoluteShareObject.put("currency", JSONObject.NULL)
            }
        }
        cashFlowDetails.put(toKey, financialShareObject)
        cashFlowDetails.remove(fromKey)
    }

    private fun migrateNewData(context: Context?) {
        val companyAssociatedDatasets = getCompanyAssociatedDatasetsForDataType(context, "new-eutaxonomy-non-financials")
        companyAssociatedDatasets.forEach {
            it.companyAssociatedData.put("dataType", "eutaxonomy-non-financials")
            context!!.connection.createStatement().execute(it.getWriteQuery())
        }
    }
}
