package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.PreparedStatement
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Migration to change the fiscal_year_end column from DATE to VARCHAR(10) and reformat existing data
 * from 'yyyy-MM-dd' to 'dd-MMM' (e.g., '2023-12-31' to '31-Dec')
 */
@Suppress("ClassName")
class V12__MigrateFiscalYearEnd : BaseJavaMigration() {
    private val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val outputFormat = DateTimeFormatter.ofPattern("dd-MMM", Locale.ENGLISH)

    override fun migrate(context: Context) {
        val connection = context.connection

        connection
            .prepareStatement(
                """
                ALTER TABLE stored_companies
                ALTER COLUMN fiscal_year_end TYPE VARCHAR(10);
                """.trimIndent(),
            ).execute()

        val selectStatement =
            connection.prepareStatement(
                """
                SELECT company_id, fiscal_year_end
                FROM stored_companies
                """.trimIndent(),
            )

        val resultSet = selectStatement.executeQuery()

        val updateStatement: PreparedStatement =
            connection.prepareStatement(
                """
                UPDATE stored_companies
                SET fiscal_year_end = ?
                WHERE company_id = ?
                """.trimIndent(),
            )

        while (resultSet.next()) {
            val companyId = resultSet.getString("company_id")
            val oldDate = resultSet.getString("fiscal_year_end")
            val formatted =
                if (companyId == null || oldDate == null) {
                    null
                } else {
                    try {
                        LocalDate.parse(oldDate, inputFormat).format(outputFormat)
                    } catch (_: Exception) {
                        null
                    }
                }
            if (formatted != null) {
                updateStatement.setString(1, formatted)
                updateStatement.setString(2, companyId)
                updateStatement.executeUpdate()
            }
        }

        resultSet.close()
        selectStatement.close()
        updateStatement.close()
    }
}
