package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial database table
 */
@Suppress("ClassName")
class V1_1__CreateInitialTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE data_requests (" +
                "data_request_id varchar(255) NOT NULL, " +
                "creation_timestamp bigint NOT NULL, " +
                "data_request_company_identifier_type varchar(255) NOT NULL, " +
                "data_request_company_identifier_value varchar(255) NOT NULL, " +
                "data_type_name varchar(255) NOT NULL, " +
                "user_id varchar(255) NOT NULL, " +
                "PRIMARY KEY (data_request_id)" +
                ")",
        )
    }
}
