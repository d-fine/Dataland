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
        val reports = "data_point_qa_reports"
        val review = "data_point_qa_review"
        val type = "data_point_type"

        if (reviewResultSet.next() && reportsResultSet.next()) {
            migrateQaTable(context, reports, type, revenue, true)
            migrateQaTable(context, reports, type, value, true)
            migrateQaTable(context, reports, type, carbon, false)
            migrateQaTable(context, review, type, revenue, true)
            migrateQaTable(context, review, type, value, true)
            migrateQaTable(context, review, type, carbon, false)
        }
    }

    /**
     * Migrates all tuples of data point ids and data point types for a fixed data point type
     * @context the context of the migration script
     * @dataPointType the data point type for the tuples to modify
     * @migrate migration script for a tuple of data point id and data point type
     */
    fun migrateQaTable(
        context: Context,
        tableName: String,
        columnName: String,
        dataPointType: String,
        cancellation: Boolean,
    ) {
        val renameMap =
            mapOf(
                "extendedCurrencyTotalRevenue" to "extendedDecimalTotalRevenueInEUR",
                "extendedCurrencyEnterpriseValue" to "extendedDecimalEnterpriseValueInEUR",
                "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue" to
                    "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
            )

        val newType =
            renameMap[dataPointType]
                ?: throw IllegalArgumentException("No renaming defined for $dataPointType")

        val selectStmt =
            context.connection.prepareStatement(
                "SELECT id, corrected_data FROM $tableName WHERE $columnName = ?",
            )
        selectStmt.setString(1, dataPointType)
        val resultSet = selectStmt.executeQuery()

        val updateSql =
            if (cancellation) {
                "UPDATE $tableName SET $columnName = ?, corrected_data = ? WHERE id = ?"
            } else {
                "UPDATE $tableName SET $columnName = ? WHERE id = ?"
            }

        val updateStmt = context.connection.prepareStatement(updateSql)

        var count = 0
        while (resultSet.next()) {
            val id = resultSet.getString("id")

            updateStmt.setString(1, newType)

            if (cancellation) {
                val correctedJson = JSONObject(resultSet.getString("corrected_data"))
                correctedJson.remove("currency")
                updateStmt.setString(2, correctedJson.toString())
                updateStmt.setString(3, id)
            } else {
                updateStmt.setString(2, id)
            }

            count += updateStmt.executeUpdate()
        }

        logger.info("Updated $count rows in $tableName from $dataPointType to $newType")
        resultSet.close()
        selectStmt.close()
        updateStmt.close()
    }
}
