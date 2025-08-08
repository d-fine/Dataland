package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script drops the "user ID --> roles" mapping table.
 * User roles are now queried from keycloak.
 */
@Suppress("ClassName")
class V1_1__DropRoleMapping : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DROP TABLE IF EXISTS api_key_entity_keycloak_roles",
        )
    }
}
