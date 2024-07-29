package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial tables
 */
class V1_1__CreateBaselineTables : BaseJavaMigration() {
    override fun migrate(context: Context) {
        context.connection.createStatement().execute(
            """
            CREATE TABLE public.review_information (
                data_id character varying(255) NOT NULL,
                message character varying(255),
                qa_status smallint,
                reception_time bigint NOT NULL,
                reviewer_keycloak_id character varying(255),
                CONSTRAINT review_information_qa_status_check CHECK (((qa_status >= 0) AND (qa_status <= 2)))
            );
            ALTER TABLE ONLY public.review_information
                ADD CONSTRAINT review_information_pkey PRIMARY KEY (data_id);
            
            CREATE TABLE public.review_queue (
                data_id character varying(255) NOT NULL,
                comment text,
                reception_time bigint NOT NULL
            );
            ALTER TABLE ONLY public.review_queue
                ADD CONSTRAINT review_queue_pkey PRIMARY KEY (data_id);
            """.trimIndent(),
        )
    }
}
