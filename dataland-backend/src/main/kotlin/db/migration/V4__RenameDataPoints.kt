package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script deletes the invitation meta info table after the corresponding feature that stores data into
 * that table has been removed.
 */
@Suppress("ClassName")
class V4__RenameDataPoints : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_point_meta_information RENAME COLUMN data_id TO data_point_id;" +
                "ALTER TABLE data_point_meta_information RENAME COLUMN data_point_identifier TO data_point_type;",
        )
    }
}
