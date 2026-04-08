package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Creates backend canonical non-sourceability lifecycle table and active uniqueness constraints.
 */
@Suppress("ClassName")
class V13__CreateNonSourceabilityInformation : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS non_sourceability_information (" +
                "non_sourceability_id UUID NOT NULL DEFAULT gen_random_uuid(), " +
                "company_id VARCHAR(255) NOT NULL, " +
                "data_type VARCHAR(255) NOT NULL, " +
                "reporting_period VARCHAR(255) NOT NULL, " +
                "qa_status VARCHAR(255) NOT NULL, " +
                "uploader_user_id VARCHAR(255) NOT NULL, " +
                "upload_time BIGINT NOT NULL, " +
                "currently_active BOOLEAN NOT NULL, " +
                "reason TEXT NOT NULL, " +
                "bypass_qa BOOLEAN NOT NULL DEFAULT FALSE, " +
                "PRIMARY KEY (non_sourceability_id)" +
                ")",
        )

        context.connection.createStatement().execute(
            "CREATE INDEX IF NOT EXISTS idx_non_sourceability_dimensions " +
                "ON non_sourceability_information(company_id, data_type, reporting_period)",
        )

        context.connection.createStatement().execute(
            "CREATE UNIQUE INDEX IF NOT EXISTS ux_non_sourceability_active_dimensions " +
                "ON non_sourceability_information(company_id, data_type, reporting_period) " +
                "WHERE currently_active = TRUE",
        )
    }
}
