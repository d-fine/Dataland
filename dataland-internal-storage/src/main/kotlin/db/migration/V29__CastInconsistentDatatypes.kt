package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration script to cast inconsistent datatypes in the database
 */
@Suppress("ClassName")
class V29__CastInconsistentDatatypes : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        // TODO: Implement migration logic to cast inconsistent datatypes
    }
}
