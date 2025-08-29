package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script resets the type of column company_alternative_names.
 * It considers the respective constraint.
 */
@Suppress("ClassName")
class V13__FixAlternativeNamesColumnTypeWithConstraint : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val stmt = context!!.connection.createStatement()

        stmt.execute(
            """
            ALTER TABLE IF EXISTS public.stored_company_entity_company_alternative_names
            DROP CONSTRAINT IF EXISTS fk9jfakb6g1jtr2mpxxbacb9cxr;
            """.trimIndent(),
        )

        stmt.execute(
            """
            ALTER TABLE public.stored_company_entity_company_alternative_names
            ALTER COLUMN company_alternative_names TYPE TEXT USING company_alternative_names::TEXT;
            """.trimIndent(),
        )

        stmt.execute(
            """
            ALTER TABLE public.stored_company_entity_company_alternative_names
            ADD CONSTRAINT fk9jfakb6g1jtr2mpxxbacb9cxr FOREIGN KEY (stored_company_entity_company_id)
            REFERENCES public.stored_companies (company_id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE NO ACTION;
            """.trimIndent(),
        )
    }
}
