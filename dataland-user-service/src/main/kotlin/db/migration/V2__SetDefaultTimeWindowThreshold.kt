package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script adds the time_window_threshold column to the portfolios table
 * and sets the default timeWindowThreshold to 'Standard' for existing monitored portfolios.
 */
@Suppress("ClassName")
class V2__SetDefaultTimeWindowThreshold : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val hasTable =
            context.connection
                .metaData
                .getTables(null, null, "portfolios", null)
                .next()

        if (!hasTable) return

        context.connection.createStatement().use { statement ->
            val addColumnSql =
                """
                ALTER TABLE portfolios ADD COLUMN IF NOT EXISTS
                time_window_threshold VARCHAR(255);
                """.trimIndent()
            statement.execute(addColumnSql)

            val updateSql =
                """
                UPDATE portfolios
                SET time_window_threshold = 'Standard'
                WHERE time_window_threshold IS NULL AND is_monitored = true;
                """.trimIndent()
            statement.execute(updateSql)
        }
    }
}
