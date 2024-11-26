package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Drops legacy table review_information containing the review QA history for older datasets
 */
@Suppress("ClassName")
class V5__DropReviewHistoryAndQueue : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DROP TABLE review_information;",
        )

        context.connection.createStatement().execute(
            "DROP TABLE review_queue;",
        )
    }
}
