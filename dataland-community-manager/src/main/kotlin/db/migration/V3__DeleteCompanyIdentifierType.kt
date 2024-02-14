package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script extends the data_requests table
 */
class V3__DeleteCompanyIdentifierType : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "DROP COLUMN data_request_company_identifier_type, " +
                "RENAME COLUMN dataRequestCompanyIdentifierValue TO datalandCompanyId"
        )
    }
}
