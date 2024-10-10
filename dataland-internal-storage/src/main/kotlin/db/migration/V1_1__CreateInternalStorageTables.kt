package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial databases
 */
@Suppress("ClassName")
class V1_1__CreateInternalStorageTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE data_items (" +
                "data_id varchar(255) NOT NULL, " +
                "data text NOT NULL, " +
                "PRIMARY KEY (data_id)" +
                ")",
        )
        context.connection.createStatement().execute(
            "CREATE TABLE blob_items (" +
                "blob_id varchar(255) NOT NULL, " +
                "data oid, " +
                "PRIMARY KEY (blob_id)" +
                ")",
        )
    }
}
