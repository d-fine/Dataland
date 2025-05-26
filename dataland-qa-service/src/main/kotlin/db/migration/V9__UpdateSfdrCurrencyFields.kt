package db.migration
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration script updates the meta information of currency related Sfdr data points, and corrects a suffix in the
 * carbon footprint.
 */
@Suppress("ClassName")
class V9__UpdateSfdrCurrencyFields : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun migrate(context: Context?) {
        val reportsResultSet =
            context!!.connection.metaData.getTables(
                null,
                null,
                "data_point_qa_reports",
                null,
            )
        val reviewResultSet =
            context!!.connection.metaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            )

        val revenue = "extendedCurrencyTotalRevenue"
        val value = "extendedCurrencyEnterpriseValue"
        val carbon = "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
        val review = "data_point_qa_review"
        val reports = "data_point_qa_reports"

        if (reportsResultSet.next() && reviewResultSet.next()) {
            currencyDeletion(context, reports, revenue)
            migrateBackendTable(context, reports, revenue)
            currencyDeletion(context, reports, value)
            migrateBackendTable(context, reports, value)
            currencyDeletion(context, reports, carbon)
            migrateBackendTable(context, reports, carbon)
            migrateBackendTable(context, review, revenue)
            migrateBackendTable(context, review, value)
            migrateBackendTable(context, review, carbon)
        }
    }

    fun currencyDeletion(
        context: Context,
        tableName: String,
        dataPointType: String,
    ) {
        val queueResultSet =
            context!!.connection.createStatement().executeQuery(
                "SELECT data_point_id, corrected_data FROM $tableName WHERE data_point_type='$dataPointType'",
            )
        val updateStatement =
            context.connection.prepareStatement(
                "UPDATE $tableName SET corrected_data = ? WHERE data_point_id = ?",
            )

        while (queueResultSet.next()) {
            val dataPointId = queueResultSet.getString("data_point_id")

            logger.info("Purging curring from data point with id $dataPointId")
            val correctedData = JSONObject(queueResultSet.getString("corrected_data"))
            correctedData.remove("currency")
            updateStatement.setString(1, correctedData.toString())
            updateStatement.setString(2, dataPointId)
            updateStatement.executeUpdate()
        }
        updateStatement.close()
        queueResultSet.close()
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
                "UPDATE $tableName SET data_point_type = ? WHERE data_point_type = ?",
            )
        updateStatement.setString(1, newType)
        updateStatement.setString(2, dataPointType)
        val count = updateStatement.executeUpdate()
        logger.info("Updated $count rows in $tableName from $dataPointType to $newType")

        updateStatement?.close()
    }
}
