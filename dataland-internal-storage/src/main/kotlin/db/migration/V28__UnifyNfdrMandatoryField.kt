package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script unifies the previous called "extendedEnumYesNoNfrdMandatory" and
 * "extendedEnumYesNoIsNfrdMandatory" to only one data point, now called "extendedEnumYesNoIsNfrdMandatory"
 */
@Suppress("ClassName")
class V28__UnifyNfdrMandatoryField : BaseJavaMigration() {
    /**
     * Renames the dataPointType if it matches a specific legacy value.
     *
     * Changes "extendedEnumYesNoNfrdMandatory" to "extendedEnumYesNoIsNfrdMandatory".
     */
    fun updateNfrdMandatory(dataPointTableEntity: DataPointTableEntity) {
        if (dataPointTableEntity.dataPointType == "extendedEnumYesNoNfrdMandatory") {
            dataPointTableEntity.dataPointType = "extendedEnumYesNoIsNfrdMandatory"
        }
    }

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val resultSet = connection.metaData.getTables(null, null, "data_point_items", null)

        if (resultSet.next()) {
            migrateDataPointTableEntities(
                context,
                "extendedEnumYesNoNfrdMandatory",
                this::updateNfrdMandatory,
            )
        }
    }
}
