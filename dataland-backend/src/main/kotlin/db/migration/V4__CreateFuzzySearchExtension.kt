package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script creates an extension which includes (among other functions) the levenshtein-distance-function.
 */
class V4__CreateFuzzySearchExtension : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;",
        )
    }
}
