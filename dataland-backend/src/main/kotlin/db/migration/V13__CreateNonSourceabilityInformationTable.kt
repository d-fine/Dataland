package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration to create the non_sourceability_information table for unified non-sourceability lifecycle.
 */
@Suppress("ClassName")
class V13__CreateNonSourceabilityInformationTable : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val connection = context.connection

        // Create the non_sourceability_information table
        connection
            .prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS non_sourceability_information (
                    non_sourceability_id UUID PRIMARY KEY,
                    company_id UUID NOT NULL,
                    data_type VARCHAR(255) NOT NULL,
                    reporting_period VARCHAR(50) NOT NULL,
                    reason TEXT NOT NULL,
                    uploader_user_id VARCHAR(255) NOT NULL,
                    upload_time TIMESTAMP WITH TIME ZONE NOT NULL,
                    qa_status VARCHAR(50) NOT NULL DEFAULT 'Pending',
                    currently_active BOOLEAN NOT NULL DEFAULT FALSE,
                    bypass_qa BOOLEAN NOT NULL DEFAULT FALSE,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """.trimIndent(),
            ).execute()

        // Create unique constraint for active requests
        connection
            .prepareStatement(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS idx_non_sourceability_active 
                ON non_sourceability_information(company_id, data_type, reporting_period) 
                WHERE qa_status IN ('Pending', 'Accepted');
                """.trimIndent(),
            ).execute()

        // Create indexes for queries
        connection
            .prepareStatement(
                """
                CREATE INDEX IF NOT EXISTS idx_non_sourceability_company 
                ON non_sourceability_information(company_id);
                """.trimIndent(),
            ).execute()

        connection
            .prepareStatement(
                """
                CREATE INDEX IF NOT EXISTS idx_non_sourceability_status 
                ON non_sourceability_information(qa_status, upload_time DESC);
                """.trimIndent(),
            ).execute()

        connection
            .prepareStatement(
                """
                CREATE INDEX IF NOT EXISTS idx_non_sourceability_data_dims 
                ON non_sourceability_information(company_id, data_type, reporting_period, qa_status);
                """.trimIndent(),
            ).execute()
    }
}
