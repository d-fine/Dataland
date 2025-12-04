package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates the company roles to the new enum values
 */
@Suppress("ClassName")
class V16__MigrateCompanyRolesWithConstraintUpdate : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val hasTable =
            context.connection
                .metaData
                .getTables(null, null, "company_role_assignments", null)
                .next()

        if (!hasTable) return

        val statement = context.connection.createStatement()
        statement.execute(
            """
            ALTER TABLE company_role_assignments
                DROP CONSTRAINT IF EXISTS company_role_assignments_role_check;
            """.trimIndent(),
        )

        statement.execute(
            """
            UPDATE company_role_assignments
            SET company_role = 'Admin'
            WHERE company_role = 'MemberAdmin';
            """.trimIndent(),
        )

        statement.execute(
            """
            UPDATE company_role_assignments
            SET company_role = 'Analyst'
            WHERE company_role = 'Member';
            """.trimIndent(),
        )

        statement.execute(
            """
            ALTER TABLE company_role_assignments
                ADD CONSTRAINT company_role_assignments_role_check
               CHECK (company_role IN ('Admin', 'Analyst', 'CompanyOwner', 'DataUploader'));
            """.trimIndent(),
        )

        statement.close()
    }
}
