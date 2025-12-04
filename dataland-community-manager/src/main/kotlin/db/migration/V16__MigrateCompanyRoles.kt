package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script migrates the company roles from MemberAdmin to Admin and Member to Analyst.
 */
@Suppress("ClassName")
class V16__MigrateCompanyRoles : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val ctx = context ?: return

        val hasTable =
            ctx.connection
                .metaData
                .getTables(null, null, "company_role_assignments", null)
                .next()

        if (hasTable) {
            val statement = ctx.connection.createStatement()

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
        }
    }
}
