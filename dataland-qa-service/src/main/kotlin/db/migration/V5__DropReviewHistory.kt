package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Drops legacy tables review_information and review_queue containing the review QA history for older datasets
 */
@Suppress("ClassName")
class V5__DropReviewHistory : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val statement = context!!.connection.createStatement()
        statement.execute("DROP TABLE review_information;")
        statement.execute("DROP TABLE review_queue;")
    }
}
