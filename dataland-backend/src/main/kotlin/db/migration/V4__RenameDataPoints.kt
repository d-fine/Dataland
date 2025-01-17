package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Change column names to new version
 */
@Suppress("ClassName")
class V4__RenameDataPoints : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val resultSet = connection.metaData.getTables(null, null, "data_point_meta_information", null)

        if (resultSet.next()) {
            connection.createStatement().execute(
                "ALTER TABLE data_point_meta_information RENAME COLUMN data_id TO data_point_id;" +
                    "ALTER TABLE data_point_meta_information RENAME COLUMN data_point_identifier TO data_point_type;",
            )
        }
    }
}
