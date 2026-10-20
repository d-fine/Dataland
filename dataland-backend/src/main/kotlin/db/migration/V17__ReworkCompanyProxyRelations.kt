package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration reworks the company_proxy_relations table:
 * - renames the company columns to child_company_id and parent_company_id,
 * - adds the reporting period interval and upload information columns,
 * - drops the framework and reporting_period columns (and with them the old unique constraint).
 * If the table does not exist yet, Hibernate creates it with the new schema and nothing is done.
 */
@Suppress("ClassName")
class V17__ReworkCompanyProxyRelations : BaseJavaMigration() {
    companion object {
        private const val TABLE = "company_proxy_relations"

        private fun renameIfExists(
            oldName: String,
            newName: String,
        ) = "DO \$\$ BEGIN " +
            "IF EXISTS (SELECT 1 FROM information_schema.columns " +
            "WHERE table_name = '$TABLE' AND column_name = '$oldName') THEN " +
            "ALTER TABLE $TABLE RENAME COLUMN $oldName TO $newName; " +
            "END IF; END \$\$"

        val statements =
            listOf(
                renameIfExists("proxied_company_id", "child_company_id"),
                renameIfExists("proxy_company_id", "parent_company_id"),
                "ALTER TABLE $TABLE ADD COLUMN IF NOT EXISTS valid_from_reporting_period varchar(255)",
                "ALTER TABLE $TABLE ADD COLUMN IF NOT EXISTS valid_to_reporting_period varchar(255)",
                "ALTER TABLE $TABLE ADD COLUMN IF NOT EXISTS upload_timestamp bigint NOT NULL DEFAULT 0",
                "ALTER TABLE $TABLE ADD COLUMN IF NOT EXISTS uploader_user_id varchar(255) NOT NULL DEFAULT 'migration'",
                "ALTER TABLE $TABLE ALTER COLUMN upload_timestamp DROP DEFAULT",
                "ALTER TABLE $TABLE ALTER COLUMN uploader_user_id DROP DEFAULT",
                "ALTER TABLE $TABLE DROP COLUMN IF EXISTS framework",
                "ALTER TABLE $TABLE DROP COLUMN IF EXISTS reporting_period",
            )
    }

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val tableExists = connection.metaData.getTables(null, null, TABLE, null).use { it.next() }
        if (!tableExists) return
        connection.createStatement().use { statement ->
            statements.forEach { statement.execute(it) }
        }
    }
}
