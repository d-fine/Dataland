package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates all sfdr datasets to match the new sfdr data model.
 */
@Suppress("ClassName")
class V25__Update_Sfdr_model : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateRateOfAccidents,
        )
    }

    private val oldToNewRateOfAccidentsKey =
        mapOf(
            "rateOfAccidentsInPercent" to "rateOfAccidents",
        )

    /**
     * Migrates sfdr data to the new sfdr data model.
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
}
