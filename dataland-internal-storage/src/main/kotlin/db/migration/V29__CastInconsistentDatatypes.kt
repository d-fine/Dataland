package db.migration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
    private val validationClassesSource = "/db/migration/V29/validationClasses.json"
    private val objectMapper =
        ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    private val validationClasses: Map<String, String> =
        objectMapper.readValue(
            javaClass.getResource(validationClassesSource).readText(),
            Map::class.java,
        ) as Map<String, String>

    /**
     * Migrates the JSON representation of a data point.
     *
     * The data point JSON is cast to the corresponding data class and back to a JSON string.
     * @param dataPointTableEntity The entity whose dataPoint field will be cast and updated.
     */
    fun castDataPoint(dataPointTableEntity: DataPointTableEntity) {
        val dataPointObject =
            objectMapper.readValue(
                dataPointTableEntity.dataPoint.getString("dataPoint"),
                Class.forName(validationClasses[dataPointTableEntity.dataPointType]),
            )
        dataPointTableEntity.dataPoint.put("dataPoint", objectMapper.writeValueAsString(dataPointObject))
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
