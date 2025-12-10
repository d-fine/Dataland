package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * This migration script updates the fiscal_year_end column in the stored_companies table
 * from the format 'yyyy-MM-dd' to 'dd-MMM'.
 */
@Suppress("ClassName")
class V12__MigrateFiscalYearEnd : BaseJavaMigration() {
    private val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val outputFormat = DateTimeFormatter.ofPattern("dd-MMM", Locale.ENGLISH)

    override fun migrate(context: Context) {
        val connection = context.connection
        val meta = connection.metaData

        val table = meta.getTables(null, null, "stored_companies", null)
        if (!table.next()) return

        val column = meta.getColumns(null, null, "stored_companies", "fiscal_year_end")
        val columnExists = column.next()
        column.close()

        if (!columnExists) {
            return
        }

        connection
            .prepareStatement(
                """
                ALTER TABLE stored_companies
                ALTER COLUMN fiscal_year_end TYPE VARCHAR(10);
                """.trimIndent(),
            ).execute()

        val select =
            connection.prepareStatement(
                "SELECT company_id, fiscal_year_end FROM stored_companies",
            )

        val update =
            connection.prepareStatement(
                "UPDATE stored_companies SET fiscal_year_end = ? WHERE company_id = ?",
            )

        val rs = select.executeQuery()
        while (rs.next()) {
            val id = rs.getString("company_id")
            val old = rs.getString("fiscal_year_end")

            val formatted =
                try {
                    LocalDate.parse(old, inputFormat).format(outputFormat)
                } catch (_: Exception) {
                    null
                }

            if (formatted != null) {
                update.setString(1, formatted)
                update.setString(2, id)
                update.executeUpdate()
            }
        }

        rs.close()
        select.close()
        update.close()
    }
}
