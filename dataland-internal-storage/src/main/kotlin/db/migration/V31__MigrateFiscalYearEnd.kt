package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.PreparedStatement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Convert date format of data_point_items where data_point_type = extendedDateFiscalYearEnd
 * from 'YYYY-MM-DD' to 'MMM-dd' (e.g. '2025-03-31' -> 'Mar-31').
 */
@Suppress("ClassName")
class V31__ConvertExtendedDateFiscalYearEnd : BaseJavaMigration() {

    private val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val outputFormat = DateTimeFormatter.ofPattern("MMM-dd")

    override fun migrate(context: Context) {
        val connection = context.connection

        val selectStatement = connection.prepareStatement(
            """
            SELECT data_point_id, data
            FROM data_point_items
            WHERE data_point_type = 'extendedDateFiscalYearEnd'
              AND data IS NOT NULL
            """.trimIndent()
        )

        val resultSet = selectStatement.executeQuery()

        val updateStatement: PreparedStatement = connection.prepareStatement(
            """
            UPDATE data_point_items
            SET data = ?
            WHERE data_point_id = ?
            """.trimIndent()
        )

        while (resultSet.next()) {
            val id = resultSet.getLong("data_point_id")
            val oldDate = resultSet.getString("data")

            val newFormatted = try {
                LocalDate.parse(oldDate, inputFormat).format(outputFormat)
            } catch (ex: Exception) {
                continue
            }

            updateStatement.setString(1, newFormatted)
            updateStatement.setLong(2, id)
            updateStatement.executeUpdate()
        }

        resultSet.close()
        selectStatement.close()
        updateStatement.close()
    }
}
