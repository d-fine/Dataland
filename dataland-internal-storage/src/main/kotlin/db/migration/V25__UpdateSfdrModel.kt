package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

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

        val excessiveCeoPayRatioInPercentValue = socialAndEmployeeMattersObject.remove(excessiveCeoPayRatioInPercentKey)
        val ceoToEmployeePayGapRatioValue = socialAndEmployeeMattersObject.remove(ceoToEmployeePayGapRatioKey)

        // how to extract value?
        val excessiveCeoPayRatioInPercentIsNumber = excessiveCeoPayRatioInPercentValue is Number
        val ceoToEmployeePayGapRatioValueisNumber = ceoToEmployeePayGapRatioValue is Number

        val newValue =
            when {
                excessiveCeoPayRatioInPercentIsNumber -> excessiveCeoPayRatioInPercentValue
                ceoToEmployeePayGapRatioValueisNumber -> ceoToEmployeePayGapRatioValue
                else -> null
            }

        socialAndEmployeeMattersObject.put(newKey, newValue)

        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
