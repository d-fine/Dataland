package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script adds the qa_reports table
 */
class V2__AddQaReports : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().execute(
            """
            CREATE TABLE public.qa_reports (
                qa_report_id character varying(255) NOT NULL,
                active boolean NOT NULL,
                data_id character varying(255) NOT NULL,
                data_type character varying(255) NOT NULL,
                qa_report text NOT NULL,
                reporter_user_id character varying(255) NOT NULL,
                upload_time bigint NOT NULL
            );
            ALTER TABLE ONLY public.qa_reports
                ADD CONSTRAINT qa_reports_pkey PRIMARY KEY (qa_report_id);
            """.trimIndent(),
        )
    }
}
