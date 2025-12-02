package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.PreparedStatement
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("ClassName")
class V31__MigrateFiscalYearEnd : BaseJavaMigration() {

    private val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val outputFormat = DateTimeFormatter.ofPattern("MMM-dd")

    override fun migrate(context: Context) {
        val connection = context.connection
        val selectStatement = connection.prepareStatement(
            """
            SELECT data_point_id, data
            FROM data_point_items
            WHERE data_point_type = 'extendedDateFiscalYearEnd'
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

            val formatted = try {
                LocalDate.parse(oldDate, inputFormat).format(outputFormat)
            } catch (_: Exception) {
                continue
            }

            updateStatement.setString(1, formatted)
            updateStatement.setLong(2, id)
            updateStatement.executeUpdate()
        }

        resultSet.close()
        selectStatement.close()
        updateStatement.close()
    }
}
