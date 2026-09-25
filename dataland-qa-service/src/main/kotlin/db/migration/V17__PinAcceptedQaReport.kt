package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/** Adds the selected report ID; existing reporter-only selections must be reselected. */
@Suppress("ClassName")
class V17__PinAcceptedQaReport : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.metaData.getTables(null, null, "dataset_judgement_entity_data_point_judgement", null).use { tables ->
            if (!tables.next()) return
        }
        context.connection.createStatement().execute(
            """
            ALTER TABLE dataset_judgement_entity_data_point_judgement
                ADD COLUMN IF NOT EXISTS accepted_qa_report_id VARCHAR(255)
            """.trimIndent(),
        )
        context.connection.metaData.getTables(null, null, "dataset_judgement", null).use { tables ->
            if (!tables.next()) return
        }
        context.connection.createStatement().execute(
            """
            UPDATE dataset_judgement_entity_data_point_judgement AS dp
            SET accepted_source = NULL, reporter_user_id_of_accepted_qa_report = NULL
            FROM dataset_judgement AS judgement
            WHERE dp.dataset_judgement_id = judgement.dataset_judgement_id
              AND judgement.judgement_state = 'Pending'
              AND dp.accepted_source = 1
              AND dp.accepted_qa_report_id IS NULL
            """.trimIndent(),
        )
    }
}
