package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates all sfdr datasets to match the new sfdr data model.
 */
@Suppress("ClassName")
class V25__UpdateSfdrModel : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateRateOfAccidents,
        )
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateExcessiveCeoPayGapRatio,
        )
    }

    /**
     * Migrates the rate of accidents in the sfdr data
     */
    fun migrateRateOfAccidents(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        val socialAndEmployeeMattersObject =
            dataset.optJSONObject("social")?.optJSONObject("socialAndEmployeeMatters") ?: return

        socialAndEmployeeMattersObject.remove("rateOfAccidentsInPercent")?.let {
            socialAndEmployeeMattersObject.put("rateOfAccidents", it)
        }

        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    /**
     * Migrates the two fields excessiveCeoPayRatioInPercent and ceoToEmployeePayGapRatio into a single field
     * called excessiveCeoPayRatio.
     */
    fun migrateExcessiveCeoPayGapRatio(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        val socialAndEmployeeMattersObject =
            dataset.optJSONObject("social")?.optJSONObject("socialAndEmployeeMatters") ?: return

        val excessiveCeoPayRatioInPercentValue =
            socialAndEmployeeMattersObject.remove("excessiveCeoPayRatioInPercent") as? JSONObject
        val ceoToEmployeePayGapRatioValue =
            socialAndEmployeeMattersObject.remove("ceoToEmployeePayGapRatio") as? JSONObject
        val valueToUse = determineValueToUse(excessiveCeoPayRatioInPercentValue, ceoToEmployeePayGapRatioValue)

        valueToUse?.let { socialAndEmployeeMattersObject.put("excessiveCeoPayRatio", it) }

        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    /**
     * Determine which value to put into the new excessiveCeoPayRatio field
     */
    private fun determineValueToUse(
        excessiveCeoPayRatioInPercentValue: JSONObject?,
        ceoToEmployeePayGapRatioValue: JSONObject?,
    ): JSONObject? {
        if (excessiveCeoPayRatioInPercentValue == null || ceoToEmployeePayGapRatioValue == null) {
            return excessiveCeoPayRatioInPercentValue ?: ceoToEmployeePayGapRatioValue
        }
        return when {
            isValueOfObjectAValidNumber(ceoToEmployeePayGapRatioValue) -> ceoToEmployeePayGapRatioValue
            isValueOfObjectAValidNumber(excessiveCeoPayRatioInPercentValue) -> excessiveCeoPayRatioInPercentValue
            else -> ceoToEmployeePayGapRatioValue
        }
    }

    /**
     * Checks if the value of an object is a valid number
     */
    private fun isValueOfObjectAValidNumber(jsonObject: JSONObject): Boolean =
        jsonObject.has("value") && jsonObject.get("value").toString().toDoubleOrNull() != null
}
