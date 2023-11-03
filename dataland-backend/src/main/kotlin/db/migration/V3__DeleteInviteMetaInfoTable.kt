package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script creates an index for a faster search for company names and identifiers
 */
class V3__DeleteInviteMetaInfoTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DROP TABLE invite_meta_info;",
        )
    }
}
