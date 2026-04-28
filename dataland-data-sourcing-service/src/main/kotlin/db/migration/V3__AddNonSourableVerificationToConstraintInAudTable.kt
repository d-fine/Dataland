package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Adds the priority column to the data_sourcing_aud table.
 *
 *
 * Before this script the table is equivalent to:
 *
 * CREATE TABLE IF NOT EXISTS public.data_sourcing_aud
 * (
 *     data_sourcing_id uuid NOT NULL,
 *     rev integer NOT NULL,
 *     revtype smallint,
 *     admin_comment character varying(1000) COLLATE pg_catalog."default",
 *     company_id uuid,
 *     data_extractor uuid,
 *     data_type character varying(255) COLLATE pg_catalog."default",
 *     date_of_next_document_sourcing_attempt date,
 *     document_collector uuid,
 *     reporting_period character varying(255) COLLATE pg_catalog."default",
 *     state character varying(255) COLLATE pg_catalog."default",
 *     priority integer,
 *     CONSTRAINT data_sourcing_aud_pkey PRIMARY KEY (rev, data_sourcing_id),
 *     CONSTRAINT fksw9ye07hoay5abvg1swy1ne9b FOREIGN KEY (rev)
 *         REFERENCES public.revinfo (rev) MATCH SIMPLE
 *         ON UPDATE NO ACTION
 *         ON DELETE NO ACTION,
 *     CONSTRAINT data_sourcing_aud_state_check CHECK (state::text = ANY (ARRAY[
 *       'Initialized'::character varying::text,
 *       'DocumentSourcing'::character varying::text,
 *       'DocumentSourcingDone'::character varying::text,
 *       'DataExtraction'::character varying::text,
 *       'DataVerification'::character varying::text,
 *       'NonSourceable'::character varying::text,
 *       'Done'::character varying::text]))
 * )
 */
@Suppress("ClassName")
class V3__AddNonSourableVerificationToConstraintInAudTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val hasTable = connection.metaData.getTables(null, null, "data_sourcing_aud", null).next()

        if (!hasTable) return

        val statement = context.connection.createStatement()
        statement.execute(
            """
            ALTER TABLE data_sourcing_aud
                DROP CONSTRAINT IF EXISTS data_sourcing_aud_state_check;
            """.trimIndent(),
        )
        statement.execute(
            """
            ALTER TABLE data_sourcing_aud
            CONSTRAINT data_sourcing_aud_state_check CHECK (state::text = ANY (ARRAY[
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
