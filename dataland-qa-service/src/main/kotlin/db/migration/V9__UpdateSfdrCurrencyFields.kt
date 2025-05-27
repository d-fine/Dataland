package db.migration
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject
import org.slf4j.LoggerFactory

/**
 * This migration updates the currency related Sfdr data points in the QA Service Data Base
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
            context.connection.metaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            )

        val revenueDataPointType = "extendedCurrencyTotalRevenue"
        val valueDataPointType = "extendedCurrencyEnterpriseValue"
        val carbonDataPointType = "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"
        val reviewTable = "data_point_qa_review"
        val reportsTable = "data_point_qa_reports"

        if (reportsResultSet.next() && reviewResultSet.next()) {
            currencyDeletion(context, reportsTable, revenueDataPointType)
            migrateBackendTable(context, reportsTable, revenueDataPointType)
            currencyDeletion(context, reportsTable, valueDataPointType)
            migrateBackendTable(context, reportsTable, valueDataPointType)
            currencyDeletion(context, reportsTable, carbonDataPointType)
            migrateBackendTable(context, reportsTable, carbonDataPointType)
            migrateBackendTable(context, reviewTable, revenueDataPointType)
            migrateBackendTable(context, reviewTable, valueDataPointType)
            migrateBackendTable(context, reviewTable, carbonDataPointType)
        }
    }

    /**
     * Deletes the currency field from corrected_data
     * @context the context of the migration script
     * @tableName the name of the table
     * @dataPointType the data point type
     */
    fun currencyDeletion(
        context: Context,
        tableName: String,
        dataPointType: String,
    ) {
        val queueResultSet =
            context.connection.createStatement().executeQuery(
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
     * Migrates all rows of backend tables corresponding to a certain data point type
     * @context the context of the migration script
     * @tableName the name of the table
     * @dataPointType the data point type
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
