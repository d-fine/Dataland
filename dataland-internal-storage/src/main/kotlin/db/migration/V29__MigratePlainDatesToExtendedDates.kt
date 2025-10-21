package db.migration

import com.fasterxml.jackson.databind.ObjectMapper
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration to convert plain date and enum fields to extended format for fiscal year fields.
 * Migration comment - re-run
 */
@Suppress("ClassName")
class V29__MigratePlainDatesToExtendedDates : BaseJavaMigration() {
    private val objectMapper = ObjectMapper()

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
                migratePlainToExtended(context, plainType, extendedType)
            }
        }
    }

    /**
     * Migrates plain data points to extended format.
     * If an extended version exists for the same company_id + reporting_period,
     * we copy its data to the plain row and delete the extended row.
     * Otherwise, we convert the plain value to extended format with null metadata.
     */
    @Suppress("MagicNumber")
    private fun migratePlainToExtended(
        context: Context,
        plainType: String,
        extendedType: String,
    ) {
        val selectStatement =
            context.connection.prepareStatement(
                """
                SELECT
                    p.data_point_id as plain_id,
                    p.data as plain_data,
                    p.company_id,
                    p.reporting_period,
                    e.data_point_id as extended_id,
                    e.data as extended_data
                FROM data_point_items p
                LEFT JOIN data_point_items e
                    ON p.company_id = e.company_id
                    AND p.reporting_period = e.reporting_period
                    AND e.data_point_type = ?
                WHERE p.data_point_type = ?
                """.trimIndent(),
            )
        selectStatement.setString(1, extendedType)
        selectStatement.setString(2, plainType)
        val resultSet = selectStatement.executeQuery()

        val updateStatement =
            context.connection.prepareStatement(
                "UPDATE data_point_items SET data = ?, data_point_type = ? WHERE data_point_id = ?",
            )

        val deleteStatement =
            context.connection.prepareStatement(
                "DELETE FROM data_point_items WHERE data_point_id = ?",
            )

        while (resultSet.next()) {
            val plainId = resultSet.getString("plain_id")
            val plainData = resultSet.getString("plain_data")
            val extendedId = resultSet.getString("extended_id")
            val extendedData = resultSet.getString("extended_data")

            val newData = if (extendedId != null) extendedData else convertToExtendedFormat(plainData)

            updateStatement.setString(1, newData)
            updateStatement.setString(2, extendedType)
            updateStatement.setString(3, plainId)
            updateStatement.executeUpdate()

            if (extendedId != null) {
                deleteStatement.setString(1, extendedId)
                deleteStatement.executeUpdate()
            }
        }

        resultSet.close()
        selectStatement.close()
        updateStatement.close()
        deleteStatement.close()
    }

    internal fun convertToExtendedFormat(plainData: String): String {
        val onceSerialized = objectMapper.readValue(plainData, String::class.java)
        val plainValue = objectMapper.readValue(onceSerialized, String::class.java)
        val extendedObject =
            objectMapper.createObjectNode().apply {
                put("value", plainValue)
                putNull("quality")
                putNull("comment")
                putNull("dataSource")
            }
        return objectMapper.writeValueAsString(objectMapper.writeValueAsString(extendedObject))
    }
}
