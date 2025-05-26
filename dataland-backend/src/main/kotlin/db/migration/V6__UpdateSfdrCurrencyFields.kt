package db.migration
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * This migration script updates the meta information of currency related Sfdr data points, and corrects a suffix in the
 * carbon footprint.
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

        val revenue = "extendedCurrencyTotalRevenue"
        val value = "extendedCurrencyEnterpriseValue"
        val carbon = "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
        val meta = "data_point_meta_information"
        val uuid = "data_point_uuid_map"
        val type = "data_point_type"
        val identifier = "data_point_identifier"

        if (metaResultSet.next()) {
            migrateBackendTable(context, meta, type, revenue)
            migrateBackendTable(context, meta, type, value)
            migrateBackendTable(context, meta, type, carbon)
            migrateBackendTable(context, uuid, identifier, revenue)
            migrateBackendTable(context, uuid, identifier, value)
            migrateBackendTable(context, uuid, identifier, carbon)
        }
    }

    /**
     * Migrates all tuples of data point ids and data point types for a fixed data point type
     * @context the context of the migration script
     * @dataPointType the data point type for the tuples to modify
     * @migrate migration script for a tuple of data point id and data point type
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
