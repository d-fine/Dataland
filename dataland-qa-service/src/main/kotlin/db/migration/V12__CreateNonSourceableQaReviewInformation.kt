package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Creates the non_sourceable_qa_review_information table in the QA service.
 * Rows are keyed by non_sourceability_id (foreign reference to the backend's
 * non_sourceability_information table) which also carries a UNIQUE constraint to
 * enforce one review record per non-sourceability entry.
 */
@Suppress("ClassName")
class V12__CreateNonSourceableQaReviewInformation : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().execute(
            """
            CREATE TABLE non_sourceable_qa_review_information (
                review_id UUID NOT NULL,
                non_sourceability_id VARCHAR(255) NOT NULL,
                company_id VARCHAR(255) NOT NULL,
                data_type VARCHAR(255) NOT NULL,
                reporting_period VARCHAR(255) NOT NULL,
                qa_status VARCHAR(50) NOT NULL,
                reason TEXT,
                uploader_user_id VARCHAR(255) NOT NULL,
                upload_time BIGINT NOT NULL,
                reviewer_user_id VARCHAR(255),
                qa_comment TEXT,
                PRIMARY KEY (review_id),
                UNIQUE (non_sourceability_id)
            )
            """.trimIndent(),
        )

        context.connection.createStatement().execute(
            """
            CREATE INDEX idx_ns_qa_review_qa_status
                ON non_sourceable_qa_review_information (qa_status)
            """.trimIndent(),
        )
    }
}
