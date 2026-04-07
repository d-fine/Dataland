package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration to create the non_sourceable_qa_review_information table for non-sourceability QA reviews.
 */
@Suppress("ClassName")
class V12__CreateNonSourceableQaReviewTable : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val connection = context.connection

        // Create the non_sourceable_qa_review_information table
        connection
            .prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS non_sourceable_qa_review_information (
                    id UUID PRIMARY KEY,
                    non_sourceability_id UUID NOT NULL,
                    company_id UUID NOT NULL,
                    data_type VARCHAR(255) NOT NULL,
                    reporting_period VARCHAR(50) NOT NULL,
                    reason TEXT NOT NULL,
                    uploader_user_id VARCHAR(255) NOT NULL,
                    upload_time TIMESTAMP WITH TIME ZONE NOT NULL,
                    qa_status VARCHAR(50) NOT NULL DEFAULT 'Pending',
                    reviewer_user_id VARCHAR(255),
                    qa_comment TEXT,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """.trimIndent(),
            ).execute()

        // Create index for event-driven lookups
        connection
            .prepareStatement(
                """
                CREATE INDEX IF NOT EXISTS idx_non_sourceable_qa_non_sourceability_id 
                ON non_sourceable_qa_review_information(non_sourceability_id);
                """.trimIndent(),
            ).execute()

        // Create index for queue view (pending tasks sorted by date)
        connection
            .prepareStatement(
                """
                CREATE INDEX IF NOT EXISTS idx_non_sourceable_qa_queue 
                ON non_sourceable_qa_review_information(qa_status, upload_time DESC);
                """.trimIndent(),
            ).execute()

        // Create index for detailed queries
        connection
            .prepareStatement(
                """
                CREATE INDEX IF NOT EXISTS idx_non_sourceable_qa_data_dims 
                ON non_sourceable_qa_review_information(company_id, data_type, reporting_period, qa_status);
                """.trimIndent(),
            ).execute()
    }
}
