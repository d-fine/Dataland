package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * The "notify me immediately" Boolean flag of DataRequestEntity objects
 * is added manually in this script, as it appears to not be added in the database
 * in case of a deployment to Clone or Prod.
 */
@Suppress("ClassName")
class V14__AddNotifyMeImmediatelyColumn : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val sql =
            """
            ALTER TABLE data_requests ADD COLUMN IF NOT EXISTS 
            notify_me_immediately BOOLEAN;
            """.trimIndent()

        context!!.connection.createStatement().use { statement ->
            statement.execute(sql)
        }
    }
}
