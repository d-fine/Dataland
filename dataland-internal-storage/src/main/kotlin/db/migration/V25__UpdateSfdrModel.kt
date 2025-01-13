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

    private val oldToNewRateOfAccidentsKey =
        mapOf(
            "rateOfAccidentsInPercent" to "rateOfAccidents",
        )

    /**
     * Migrates the rate of accidents in the sfdr data
     */
    fun migrateRateOfAccidents(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        val socialObject = dataset.optJSONObject("social") ?: return
        val socialAndEmployeeMattersObject = socialObject.optJSONObject("socialAndEmployeeMatters") ?: return

        for ((oldKey, newKey) in oldToNewRateOfAccidentsKey) {
            val oldValue = socialAndEmployeeMattersObject.remove(oldKey) ?: continue
            socialAndEmployeeMattersObject.put(newKey, oldValue)
        }

        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }

    /**
     * Migrates the excessive ceo pay ratio in the sfdr data
     */
    fun migrateExcessiveCeoPayGapRatio(dataTableEntity: DataTableEntity) {
        val excessiveCeoPayRatioInPercentKey = "excessiveCeoPayRatioInPercent"
        val ceoToEmployeePayGapRatioKey = "ceoToEmployeePayGapRatio"
        val newKey = "excessiveCeoPayRatio"

        val dataset = dataTableEntity.dataJsonObject

        val socialObject = dataset.optJSONObject("social") ?: return
        val socialAndEmployeeMattersObject = socialObject.optJSONObject("socialAndEmployeeMatters") ?: return

        val excessiveCeoPayRatioInPercentValue = socialAndEmployeeMattersObject.remove(excessiveCeoPayRatioInPercentKey) as? JSONObject
        val ceoToEmployeePayGapRatioValue = socialAndEmployeeMattersObject.remove(ceoToEmployeePayGapRatioKey) as? JSONObject

        if (excessiveCeoPayRatioInPercentValue != null && ceoToEmployeePayGapRatioValue == null) {
            socialAndEmployeeMattersObject.put(newKey, excessiveCeoPayRatioInPercentValue)
        }

        if (excessiveCeoPayRatioInPercentValue == null && ceoToEmployeePayGapRatioValue != null) {
            socialAndEmployeeMattersObject.put(newKey, ceoToEmployeePayGapRatioValue)
        }

        if (excessiveCeoPayRatioInPercentValue != null && ceoToEmployeePayGapRatioValue != null) {
            if (ceoToEmployeePayGapRatioValue.has("value")) {
                socialAndEmployeeMattersObject.put(newKey, ceoToEmployeePayGapRatioValue)
            } else if (excessiveCeoPayRatioInPercentValue.has("value")) {
                socialAndEmployeeMattersObject.put(newKey, excessiveCeoPayRatioInPercentValue)
            } else {
                socialAndEmployeeMattersObject.put(newKey, ceoToEmployeePayGapRatioValue)
            }
        }

        if (excessiveCeoPayRatioInPercentValue != null || ceoToEmployeePayGapRatioValue != null) {
            dataTableEntity.companyAssociatedData.put("data", dataset.toString())
        }
    }
}
