package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Adds storage for automatic pre-approval check results to data point judgements.
 */
@Suppress("ClassName")
class V14__AddPreApprovalCheckResultsToDataPointJudgement : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().execute(
            """
            ALTER TABLE dataset_judgement_entity_data_point_judgement
                ADD COLUMN pre_approval_check_results TEXT
            """.trimIndent(),
        )
    }
}
