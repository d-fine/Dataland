package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script renames data point types and removes the 'provider' field if present.
 */
@Suppress("ClassName")
class V29__RenameAndFixPcafEntries : BaseJavaMigration() {
    companion object {
        val renameMap =
            mapOf(
                "customEnumPcafMainSector" to "extendedEnumPcafMainSector",
                "customEnumCompanyExchangeStatus" to "extendedEnumCompanyExchangeStatus",
            )
    }

    /**
     * Updates the data point type based on the rename map and removes the 'provider' field.
     */
    fun updateDataTableEntity(entity: DataPointTableEntity) {
        entity.dataPointType = renameMap.getOrDefault(entity.dataPointType, entity.dataPointType)
        entity.dataPoint.remove("provider")
    }

    override fun migrate(context: Context?) {
        renameMap.keys.forEach {
            migrateDataPointTableEntities(context, it, ::updateDataTableEntity)
        }
    }
}
