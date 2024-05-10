package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates the request status history to a separate table
 */
public class V7__MigrateRequestStatusHistory: BaseJavaMigration() {

    override fun migrate(context: Context?) {
        // todo
    }
}
