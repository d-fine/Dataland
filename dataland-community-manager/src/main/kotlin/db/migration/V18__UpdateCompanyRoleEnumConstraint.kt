package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration that is used to drop the constraint on the company role assignment of the company_role_assignments table.
 */
@Suppress("ClassName")
class V18__UpdateCompanyRoleEnumConstraint : BaseJavaMigration() {
    override fun migrate(context: Context) {
        val hasTable =
            context.connection
                .metaData
                .getTables(null, null, "company_role_assignments", null)
                .next()

        if (!hasTable) return

        val sql =
            """
            ALTER TABLE company_role_assignments
                DROP CONSTRAINT IF EXISTS company_role_assignments_role_check;

            ALTER TABLE company_role_assignments
                ADD CONSTRAINT company_role_assignments_role_check
               CHECK (company_role IN ('Admin', 'Analyst', 'CompanyOwner', 'DataUploader'));
            """.trimIndent()

        context.connection.createStatement().use { statement ->
            statement.execute(sql)
        }
    }
}
