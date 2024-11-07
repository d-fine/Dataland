package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration that is used to drop the constraint on the framework property of the elementary_events table.
 */
@Suppress("ClassName")
class V9__DropDataTypeEnumConstraint : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val sql =
            """
            ALTER TABLE elementary_events
                DROP CONSTRAINT IF EXISTS elementary_events_framework_check;
            """.trimIndent()

        context.connection.createStatement().use { statement ->
            statement.execute(sql)
        }
    }
}
