package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates the existing EU taxonomy non-financials datasets and migrates all
 * existing BaseDataPoints to ExtendedDataPoints.
 */
@Suppress("ClassName")
class V25__RenameDataPoints : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val resultSet = connection.metaData.getTables(null, null, "data_point_items", null)

        if (resultSet.next()) {
            connection.createStatement().execute(
                "ALTER TABLE data_point_items RENAME COLUMN data_id TO data_point_id;" +
                    "ALTER TABLE data_point_items RENAME COLUMN data_point_identifier TO data_point_type;",
            )
        }
    }
}
