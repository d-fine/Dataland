package db.migration

import db.migration.utils.DataPointTableEntity
import db.migration.utils.getAllDataPointTypes
import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.math.BigInteger

/**
 * Migration script to cast inconsistent datatypes in the database
 */
@Suppress("ClassName")
class V29__CastInconsistentDatatypes : BaseJavaMigration() {
    /**
     * Migrates the JSON representation of a data point.
     *
     * The value of a data point JSON is cast to Integer or double based on its content.
     * If the value cannot be cast to either type, it remains unchanged.
     * @param dataPointTableEntity The entity whose dataPoint field will be cast and updated.
     */
    fun castDataPointValue(dataPointTableEntity: DataPointTableEntity) {
        try {
            val datapointValue = dataPointTableEntity.dataPoint.get("value") as BigInteger
            dataPointTableEntity.dataPoint.put("value", datapointValue)
        } catch (e: ClassCastException) {
            println("Failed to cast value to BigInteger: ${e.message}")
            try {
                val datapointValue = dataPointTableEntity.dataPoint.get("value") as Double
                dataPointTableEntity.dataPoint.put("value", datapointValue)
            } catch (e: ClassCastException) {
                println("Failed to cast value to Double: ${e.message}")
                // If the value is neither a BigInteger nor a Double, we leave it as is.
                return
            }
        }
    }

    /**
     * Migrates the JSON representation of a data point.
     *
     * Removes the "value" field from the data point JSON if its value is the string "null".
     * @param dataPointTableEntity The entity whose dataPoint field will be cast and updated.
     */
    fun removeNullValues(dataPointTableEntity: DataPointTableEntity) {
        val datapointValue = dataPointTableEntity.dataPoint.get("value")
        if (datapointValue == "null") {
            dataPointTableEntity.dataPoint.remove("value")
        }
    }

    /**
     * Migrates the JSON representation of a data point.
     *
     * Removes the "value" field from the data point JSON if its value is the string "null".
     * @param dataPointTableEntity The entity whose dataPoint field will be cast and updated.
     */
    fun castPagesToString(dataPointTableEntity: DataPointTableEntity) {
        val datapointPage = dataPointTableEntity.dataPoint.get("page")

        if (datapointPage is String) {
            return
        } else {
            dataPointTableEntity.dataPoint.put("page", datapointPage.toString())
        }
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
                    this::castDataPointValue,
                )
                migrateDataPointTableEntities(
                    context,
                    dataPointType,
                    this::removeNullValues,
                )
                migrateDataPointTableEntities(
                    context,
                    dataPointType,
                    this::castPagesToString,
                )
            }
        }
    }
}
