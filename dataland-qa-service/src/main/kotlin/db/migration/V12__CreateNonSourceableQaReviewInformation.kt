package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Creates QA non-sourceability review table keyed by backend nonSourceabilityId.
 */
@Suppress("ClassName")
class V12__CreateNonSourceableQaReviewInformation : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS non_sourceable_qa_review_information (" +
                "non_sourceability_id UUID NOT NULL, " +
                "company_id VARCHAR(255) NOT NULL, " +
                "data_type VARCHAR(255) NOT NULL, " +
                "reporting_period VARCHAR(255) NOT NULL, " +
                "qa_status VARCHAR(255) NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "uploader_user_id VARCHAR(255) NOT NULL, " +
                "upload_time BIGINT NOT NULL, " +
                "reviewer_user_id VARCHAR(255), " +
                "qa_comment TEXT, " +
                "PRIMARY KEY (non_sourceability_id)" +
                ")",
        )

        context.connection.createStatement().execute(
            "CREATE INDEX IF NOT EXISTS idx_non_sourceable_qa_status " +
                "ON non_sourceable_qa_review_information(qa_status)",
        )

        context.connection.createStatement().execute(
            "CREATE INDEX IF NOT EXISTS idx_non_sourceable_qa_dimensions " +
                "ON non_sourceable_qa_review_information(company_id, data_type, reporting_period)",
        )
    }
}
