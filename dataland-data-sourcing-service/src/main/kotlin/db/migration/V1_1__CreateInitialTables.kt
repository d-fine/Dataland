package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Creates the initial database tables for the data sourcing service.
 * Uses IF NOT EXISTS to handle existing databases that were created before Flyway was introduced.
 */
@Suppress("ClassName")
class V1_1__CreateInitialTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS data_sourcing (" +
                "data_sourcing_id uuid NOT NULL, " +
                "company_id uuid NOT NULL, " +
                "reporting_period varchar(255) NOT NULL, " +
                "data_type varchar(255) NOT NULL, " +
                "state varchar(255) NOT NULL, " +
                "date_of_next_document_sourcing_attempt date, " +
                "document_collector uuid, " +
                "data_extractor uuid, " +
                "admin_comment varchar(1000), " +
                "PRIMARY KEY (data_sourcing_id), " +
                "CONSTRAINT uq_data_sourcing UNIQUE (company_id, reporting_period, data_type)" +
                ")",
        )
        context.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS requests (" +
                "id uuid NOT NULL, " +
                "company_id uuid NOT NULL, " +
                "reporting_period varchar(255) NOT NULL, " +
                "data_type varchar(255) NOT NULL, " +
                "user_id uuid NOT NULL, " +
                "creation_time_stamp bigint NOT NULL, " +
                "member_comment varchar(1000), " +
                "admin_comment varchar(1000), " +
                "last_modified_date bigint NOT NULL, " +
                "request_priority varchar(255) NOT NULL, " +
                "state varchar(255) NOT NULL, " +
                "data_sourcing_id uuid, " +
                "PRIMARY KEY (id)" +
                ")",
        )
    }
}
