package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates the existing SFDR datasets, more specifically it introduces an additional layer to
 * each sector in the high-impact climate sectors
 */
@Suppress("ClassName")
class V12__MigrateHighImpactClimateSectorsSfdr : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateSfdrHighImpactClimateSectors,
        )
    }

    /**
     * Migrate the extended data points corresponding to nace codes in high impact climate sectors to an additional
     * layer with the new field named "highImpactClimateSectorEnergyConsumptionInGWh"
     */
    fun migrateSfdrHighImpactClimateSectors(dataTableEntity: DataTableEntity) {
        val sectorsToMigrate = emptyMap<String, JSONObject>().toMutableMap()
        val dataset = dataTableEntity.dataJsonObject
        val applicableHighImpactClimateSectorsObject =
            (
                (dataset.getOrJavaNull("environmental") as JSONObject?)
                    ?.getOrJavaNull("energyPerformance") as JSONObject?
            )?.getOrJavaNull("applicableHighImpactClimateSectors") as JSONObject?
        if (applicableHighImpactClimateSectorsObject !== null) {
            applicableHighImpactClimateSectorsObject.keys().forEach { naceCode ->
                if (applicableHighImpactClimateSectorsObject.has(naceCode)) {
                    sectorsToMigrate[naceCode] = applicableHighImpactClimateSectorsObject.get(naceCode) as JSONObject
                }
            }
            sectorsToMigrate.forEach { (sector, extendedDataPoint) ->
                applicableHighImpactClimateSectorsObject.remove(sector)
                applicableHighImpactClimateSectorsObject.put(
                    sector.removeSuffix("InGWh"),
                    JSONObject(mapOf("highImpactClimateSectorEnergyConsumptionInGWh" to extendedDataPoint)),
                )
            }
        }
        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
