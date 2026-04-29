package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Creates the non_sourceability_information table which serves as the canonical
 * backend persistence for the unified non-sourceability lifecycle.
 *
 * SourceabilityEntity / data_sourceability is retained as backup-only and not modified.
 */
@Suppress("ClassName")
class V13__CreateNonSourceabilityInformation : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().execute(
            """
            CREATE TABLE non_sourceability_information (
                non_sourceability_id UUID NOT NULL,
                company_id VARCHAR(255) NOT NULL,
                data_type VARCHAR(255) NOT NULL,
                reporting_period VARCHAR(255) NOT NULL,
                qa_status VARCHAR(50) NOT NULL,
                uploader_user_id VARCHAR(255) NOT NULL,
                upload_time BIGINT NOT NULL,
                currently_active BOOLEAN NOT NULL,
                reason TEXT,
                bypass_qa BOOLEAN NOT NULL,
                PRIMARY KEY (non_sourceability_id)
            )
            """.trimIndent(),
        )

        context.connection.createStatement().execute(
            """
            CREATE INDEX idx_non_sourceability_tuple
                ON non_sourceability_information (company_id, data_type, reporting_period)
            """.trimIndent(),
        )

        context.connection.createStatement().execute(
            """
            CREATE INDEX idx_non_sourceability_qa_status
                ON non_sourceability_information (qa_status)
            """.trimIndent(),
        )

        context.connection.createStatement().execute(
            """
            CREATE UNIQUE INDEX idx_non_sourceability_active_unique
                ON non_sourceability_information (company_id, data_type, reporting_period)
                WHERE currently_active = TRUE
            """.trimIndent(),
        )
    }
}
