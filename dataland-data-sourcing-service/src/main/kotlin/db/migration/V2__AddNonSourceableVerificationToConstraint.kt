package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Extends constraint in data_sourcing table to include NonSourceableVerification state.
 *
 *
 * Before this script the table is equivalent to:
 *
 * CREATE TABLE IF NOT EXISTS public.data_sourcing
 * (
 *     data_sourcing_id uuid NOT NULL,
 *     admin_comment character varying(1000) COLLATE pg_catalog."default",
 *     company_id uuid,
 *     data_extractor uuid,
 *     data_type character varying(255) COLLATE pg_catalog."default",
 *     date_of_next_document_sourcing_attempt date,
 *     document_collector uuid,
 *     reporting_period character varying(255) COLLATE pg_catalog."default",
 *     state character varying(255) COLLATE pg_catalog."default",
 *     priority integer NOT NULL DEFAULT 10,
 *     CONSTRAINT data_sourcing_pkey PRIMARY KEY (data_sourcing_id),
 *     CONSTRAINT uksjko153iysj8akf5l5l86phx8 UNIQUE (company_id, reporting_period, data_type),
 *     CONSTRAINT data_sourcing_state_check
 *       CHECK (state::text = ANY (
 *         ARRAY[
 *           'Initialized'::character varying::text,
 *           'DocumentSourcing'::character varying::text,
 *           'DocumentSourcingDone'::character varying::text,
 *           'DataExtraction'::character varying::text,
 *           'DataVerification'::character varying::text,
 *           'NonSourceable'::character varying::text,
 *           'Done'::character varying::text
 *         ]
 *       ))
 * )
 */
@Suppress("ClassName")
class V2__AddNonSourceableVerificationToConstraint : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val hasTable = connection.metaData.getTables(null, null, "data_sourcing", null).next()

        if (!hasTable) return

        val statement = context.connection.createStatement()
        statement.execute(
            """
            ALTER TABLE data_sourcing
                DROP CONSTRAINT IF EXISTS data_sourcing_state_check;
            """.trimIndent(),
        )
        statement.execute(
            """
            ALTER TABLE data_sourcing
                ADD CONSTRAINT data_sourcing_state_check 
                CHECK (state::text = ANY (ARRAY[
                'Initialized'::character varying::text, 
                'DocumentSourcing'::character varying::text, 
                'DocumentSourcingDone'::character varying::text, 
                'DataExtraction'::character varying::text, 
                'DataVerification'::character varying::text, 
                'NonSourceable'::character varying::text, 
                'NonSourceableVerification'::character varying::text, 
                'Done'::character varying::text]))
            """.trimIndent(),
        )
        statement.close()
    }
}
