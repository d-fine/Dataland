package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Create the table holding the human log of companies with incomplete fiscal year end or sector information
 */
@Suppress("ClassName")
class V4__CreateIncompleteCompanyInformation : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS incomplete_company_information (" +
                "company_id VARCHAR(255) PRIMARY KEY, " +
                "creation_timestamp BIGINT NOT NULL)",
        )
    }
}
