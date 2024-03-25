package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial databases
 */
class V3__AddVirusScanStatus : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        addVirusScanType(context!!)
    }

    private fun addVirusScanType(context: Context) {
        // TODO what should be the migrated state? "Accepted" or a whole new one: "unscanned"?
        context.connection.createStatement().execute(
            """  
            ALTER TABLE document_meta_info
            ADD COLUMN virus_scan_status varchar(255) NOT NULL DEFAULT 'Unscanned'
            """.trimIndent(),
        )
    }
}
