package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Removes the estimated PCAF data point types from the backend's data point metadata tables, as estimations are no
 * longer part of the PCAF data model. The data point values themselves live in dataland-internal-storage.
 */
@Suppress("ClassName")
class V18__RemovePcafEstimationDataPointTypes : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        val deletedDataPointTypes =
            listOf(
                "extendedDecimalEstimatedMarketCapitalizationInEUR",
                "extendedDecimalEstimatedBookValueOfDebtInEUR",
                "extendedDecimalEstimatedMinoritiesInterestInEUR",
                "extendedDecimalEstimatedTotalEquityAndDebtInEUR",
                "extendedDecimalEstimatedScope1GhgEmissionsInTonnes",
                "extendedDecimalEstimatedScope2GhgEmissionsLocationBasedInTonnes",
                "extendedDecimalEstimatedScope2GhgEmissionsMarketBasedInTonnes",
                "extendedDecimalEstimatedScope3GhgEmissionsInTonnes",
            )
    }

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        mapOf(
            "data_point_meta_information" to "data_point_type",
            "data_point_uuid_map" to "data_point_identifier",
        ).forEach { (table, column) ->
            if (connection.metaData.getTables(null, null, table, null).next()) {
                deletedDataPointTypes.forEach { deleteDataPointType(context, table, column, it) }
            }
        }
    }

    /**
     * Deletes all rows matching a data point type in the selected table and column.
     */
    fun deleteDataPointType(
        context: Context,
        tableName: String,
        columnName: String,
        dataPointType: String,
    ) {
        context.connection.prepareStatement("DELETE FROM $tableName WHERE $columnName = ?").use { statement ->
            statement.setString(1, dataPointType)
            logger.info("Deleted ${statement.executeUpdate()} rows in $tableName with \"$dataPointType\"")
        }
    }
}
