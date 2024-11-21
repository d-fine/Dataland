package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the QA review database
 */
@Suppress("ClassName")
class V3__CreateQaTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS qa_review (" +
                "event_id UUID NOT NULL DEFAULT gen_random_uuid() , " +
                "data_id VARCHAR(255) NOT NULL, " +
                "company_id VARCHAR(255) NOT NULL, " +
                "company_name VARCHAR(255) NOT NULL, " +
                "data_type VARCHAR(255) NOT NULL, " +
                "reporting_period VARCHAR(255) NOT NULL, " +
                "timestamp smallint NOT NULL, " +
                "qa_status VARCHAR(255) NOT NULL, " +
                "triggering_user_id VARCHAR(255) NOT NULL, " +
                "comment VARCHAR(255), " +
                "PRIMARY KEY (event_id)" +
                ")",
        )

        context.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS review_queue (" +
                "data_id VARCHAR(255) NOT NULL, " +
                "reception_time smallint NOT NULL, " +
                "comment text, " +
                "company_id VARCHAR(255) NOT NULL, " +
                "company_name VARCHAR(255) NOT NULL," +
                "framework VARCHAR(255) NOT NULL, " +
                "reporting_period VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (data_id)" +
                ")",
        )
    }
}
