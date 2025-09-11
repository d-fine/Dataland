package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.getAllDataPointTypes
import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration script to cast inconsistent datatypes in the database
 */
@Suppress("ClassName")
class V29__CastInconsistentDatatypes : BaseJavaMigration() {
    fun castDataPoint(dataPointTableEntity: DataPointTableEntity) {
        // TODO: Implement casting logic for inconsistent datatypes
    }

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val resultSet = connection.metaData.getTables(null, null, "data_point_items", null)

        if (resultSet.next()) {
            val dataPointTypes = getAllDataPointTypes(context)
            dataPointTypes.forEach { dataPointType ->
                migrateDataPointTableEntities(
                    context,
                    dataPointType,
                    this::castDataPoint,
                )
            }
        }
    }
}
