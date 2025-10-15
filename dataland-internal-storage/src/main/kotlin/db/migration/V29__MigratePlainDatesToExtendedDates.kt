package db.migration

import db.migration.utils.migrateDataPointTableEntities
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration to convert plain date and enum fields to extended format for fiscal year fields.
 */
@Suppress("ClassName")
class V29__MigratePlainDatesToExtendedDates : BaseJavaMigration() {
    private val plainToExtendedMappings =
        mapOf(
            "plainDateFiscalYearEnd" to "extendedDateFiscalYearEnd",
            "plainEnumFiscalYearDeviation" to "extendedEnumFiscalYearDeviation",
        )

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val resultSet = connection.metaData.getTables(null, null, "data_point_items", null)

        if (resultSet.next()) {
            plainToExtendedMappings.forEach { (plainType, extendedType) ->
                migrateDataPointTableEntities(context, plainType) { dataPoint ->
                    dataPoint.dataPointType = extendedType
                }
            }
        }
    }
}
