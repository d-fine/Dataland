package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial databases
 */
@Suppress("ClassName")
class V1_1__CreateBackendTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        createCompanyTables(context!!)
        context.connection.createStatement().execute(
            "CREATE TABLE data_meta_information (" +
                "data_id varchar(255) NOT NULL, " +
                "currently_active boolean, " +
                "data_type varchar(255) NOT NULL, " +
                "quality_status smallint NOT NULL, " +
                "reporting_period varchar(255) NOT NULL, " +
                "upload_time bigint NOT NULL, " +
                "uploader_user_id varchar(255) NOT NULL, " +
                "company_id varchar(255), " +
                "PRIMARY KEY (data_id)" +
                ")",
        )
        context.connection.createStatement().execute(
            "CREATE TABLE invite_meta_info (" +
                "invite_id varchar(255) NOT NULL, " +
                "file_id varchar(255), " +
                "invite_result_message varchar(255), " +
                "timestamp bigint, " +
                "user_id varchar(255), " +
                "was_invite_successful boolean, " +
                "PRIMARY KEY (invite_id)" +
                ")",
        )
    }

    private fun createCompanyTables(context: Context) {
        context.connection.createStatement().execute(
            "CREATE TABLE stored_companies (" +
                "company_id varchar(255) NOT NULL, " +
                "company_legal_form varchar(255), " +
                "company_name varchar(255), " +
                "country_code varchar(255), " +
                "headquarters varchar(255), " +
                "headquarters_postal_code varchar(255), " +
                "is_teaser_company boolean, " +
                "sector varchar(255), " +
                "website varchar(255), " +
                "PRIMARY KEY (company_id)" +
                ")",
        )
        context.connection.createStatement().execute(
            "CREATE TABLE company_identifiers (" +
                "identifier_type varchar(255) NOT NULL, " +
                "identifier_value varchar(255) NOT NULL, " +
                "company_id varchar(255) NOT NULL, " +
                "PRIMARY KEY (identifier_type, identifier_value)" +
                ")",
        )
        context.connection.createStatement().execute(
            "CREATE TABLE stored_company_entity_company_alternative_names (" +
                "stored_company_entity_company_id varchar(255) NOT NULL, " +
                "company_alternative_names varchar(255)" +
                ")",
        )
    }
}
