package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Adds storage for automatic pre-approval check results to data point judgements.
 */
@Suppress("ClassName")
class V14__AddPreApprovalCheckResultsToDataPointJudgement : BaseJavaMigration() {
    companion object {
        const val DATA_POINT_JUDGEMENT_TABLE = "dataset_judgement_entity_data_point_judgement"
        const val PRE_APPROVAL_CHECK_RESULTS_COLUMN = "pre_approval_check_results"
    }

    override fun migrate(context: Context) {
        val metaData = context.connection.metaData
        if (!metaData.getTables(null, null, DATA_POINT_JUDGEMENT_TABLE, null).next() ||
            metaData.getColumns(null, null, DATA_POINT_JUDGEMENT_TABLE, PRE_APPROVAL_CHECK_RESULTS_COLUMN).next()
        ) {
            return
        }

        context.connection.createStatement().execute(
            """
            ALTER TABLE $DATA_POINT_JUDGEMENT_TABLE
                ADD COLUMN $PRE_APPROVAL_CHECK_RESULTS_COLUMN TEXT
            """.trimIndent(),
        )
    }
}
