package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script deletes the invitation meta info table after the corresponding feature that stores data into
 * that table has been removed.
 */
@Suppress("ClassName")
class V3__DeleteInviteMetaInfoTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DROP TABLE invite_meta_info;",
        )
    }
}
