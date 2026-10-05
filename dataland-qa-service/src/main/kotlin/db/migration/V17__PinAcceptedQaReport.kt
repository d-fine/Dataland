package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.Connection

/**
 * Adds the column storing the exact QA report accepted by the reviewer.
 *
 * Pending judgements may still contain legacy QA selections that only identify the reporter:
 *  1. If that reporter has exactly one QA report for the data point, the selection is pinned to it.
 *  2. Otherwise (no or several reports) the selection is reset and has to be reselected.
 */
@Suppress("ClassName")
class V17__PinAcceptedQaReport : BaseJavaMigration() {
    private companion object {
        const val DATA_POINT_JUDGEMENT_TABLE = "dataset_judgement_entity_data_point_judgement"
        const val DATASET_JUDGEMENT_TABLE = "dataset_judgement"

        const val ADD_COLUMN = """
            ALTER TABLE $DATA_POINT_JUDGEMENT_TABLE
                ADD COLUMN IF NOT EXISTS accepted_qa_report_id VARCHAR(255)
        """

        const val LEGACY_PENDING_QA_SELECTION = """
            dp.dataset_judgement_id = judgement.dataset_judgement_id
              AND judgement.judgement_state = 'Pending'
              AND dp.accepted_source = 1
              AND dp.accepted_qa_report_id IS NULL
        """

        const val REPORTS_OF_SELECTED_REPORTER = """
            FROM data_point_qa_reports AS report
            WHERE report.data_point_id = dp.data_point_id
              AND report.reporter_user_id = dp.reporter_user_id_of_accepted_qa_report::text
        """

        const val PIN_UNAMBIGUOUS_SELECTIONS = """
            UPDATE $DATA_POINT_JUDGEMENT_TABLE AS dp
            SET accepted_qa_report_id = (SELECT report.qa_report_id $REPORTS_OF_SELECTED_REPORTER)
            FROM $DATASET_JUDGEMENT_TABLE AS judgement
            WHERE $LEGACY_PENDING_QA_SELECTION
              AND (SELECT COUNT(*) $REPORTS_OF_SELECTED_REPORTER) = 1
        """

        const val RESET_REMAINING_SELECTIONS = """
            UPDATE $DATA_POINT_JUDGEMENT_TABLE AS dp
            SET accepted_source = NULL, reporter_user_id_of_accepted_qa_report = NULL
            FROM $DATASET_JUDGEMENT_TABLE AS judgement
            WHERE $LEGACY_PENDING_QA_SELECTION
        """
    }

    override fun migrate(context: Context) {
        val connection = context.connection
        if (!tableExists(connection, DATA_POINT_JUDGEMENT_TABLE)) return
        connection.createStatement().use { statement ->
            statement.execute(ADD_COLUMN)
            if (tableExists(connection, DATASET_JUDGEMENT_TABLE)) {
                statement.execute(PIN_UNAMBIGUOUS_SELECTIONS)
                statement.execute(RESET_REMAINING_SELECTIONS)
            }
        }
    }

    private fun tableExists(
        connection: Connection,
        table: String,
    ): Boolean = connection.metaData.getTables(null, null, table, null).use { it.next() }
}
