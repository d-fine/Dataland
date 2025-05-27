package db.migration
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * This migration script updates the meta information and uuid mapping of currency related Sfdr data points.
 */
@Suppress("ClassName")
class V6__UpdateSfdrCurrencyFields : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun migrate(context: Context?) {
        val metaResultSet =
            context!!.connection.metaData.getTables(
                null,
                null,
                "data_point_meta_information",
                null,
            )

        val revenueDataPointType = "extendedCurrencyTotalRevenue"
        val valueDataPointType = "extendedCurrencyEnterpriseValue"
        val carbonDataPointType = "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
        val metaTable = "data_point_meta_information"
        val uuidTable = "data_point_uuid_map"
        val typeColumn = "data_point_type"
        val identifierColumn = "data_point_identifier"

        if (metaResultSet.next()) {
            migrateBackendTable(context, metaTable, typeColumn, revenueDataPointType)
            migrateBackendTable(context, metaTable, typeColumn, valueDataPointType)
            migrateBackendTable(context, metaTable, typeColumn, carbonDataPointType)
            migrateBackendTable(context, uuidTable, identifierColumn, revenueDataPointType)
            migrateBackendTable(context, uuidTable, identifierColumn, valueDataPointType)
            migrateBackendTable(context, uuidTable, identifierColumn, carbonDataPointType)
        }
    }

    /**
     * Updates a row in the backend tables.
     * @context the context of the migration script
     * @tableName the name of the table
     * @dataPointType the data point type
     */
    fun migrateBackendTable(
        context: Context,
        tableName: String,
        columnName: String,
        dataPointType: String,
    ) {
        val renameMap =
            mapOf(
                "extendedCurrencyTotalRevenue" to "extendedDecimalTotalRevenueInEUR",
                "extendedCurrencyEnterpriseValue" to "extendedDecimalEnterpriseValueInEUR",
                "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
                    to "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
            )

        val newType =
            renameMap[dataPointType]
                ?: throw IllegalArgumentException("No renaming defined for $dataPointType")

        val updateStatement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET $columnName = ? WHERE $columnName = ?",
            )
        updateStatement.setString(1, newType)
        updateStatement.setString(2, dataPointType)
        val count = updateStatement.executeUpdate()
        logger.info("Updated $count rows in $tableName from $dataPointType to $newType")

        updateStatement?.close()
    }
}
