package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script creates an index for a faster search for data point QA updates
 */
@Suppress("ClassName")
class V8__CreateQaDataPointIndices : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            """
            CREATE TABLE IF NOT EXISTS data_point_qa_review
            (
                event_id uuid NOT NULL,
                comment text COLLATE pg_catalog."default",
                company_id character varying(255) COLLATE pg_catalog."default" NOT NULL,
                company_name character varying(255) COLLATE pg_catalog."default" NOT NULL,
                data_point_id character varying(255) COLLATE pg_catalog."default" NOT NULL,
                data_point_type character varying(255) COLLATE pg_catalog."default" NOT NULL,
                qa_status character varying(255) COLLATE pg_catalog."default" NOT NULL,
                reporting_period character varying(255) COLLATE pg_catalog."default" NOT NULL,
                "timestamp" bigint NOT NULL,
                triggering_user_id character varying(255) COLLATE pg_catalog."default" NOT NULL,
                CONSTRAINT data_point_qa_review_pkey PRIMARY KEY (event_id)
            );
            """.trimIndent(),
        )

        context!!.connection.createStatement().execute(
            "CREATE INDEX data_point_id_idx ON data_point_qa_review (data_point_id);" +
                "CREATE INDEX company_id_idx ON data_point_qa_review (company_id);" +
                "CREATE INDEX qa_status_idx ON data_point_qa_review (qa_status);",
        )
    }
}
