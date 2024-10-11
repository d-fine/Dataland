package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script extends the data_requests table
 */
@Suppress("ClassName")
class V5__DeleteCompanyIdentifierType : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "DELETE FROM data_requests " +
                "WHERE data_request_company_identifier_type <> 'DatalandCompanyId'",
        )
        context.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "RENAME COLUMN data_request_company_identifier_value TO dataland_company_id",
        )
        context.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "DROP COLUMN data_request_company_identifier_type",
        )
    }
}
