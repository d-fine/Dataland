package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script extends the data_requests table
 */
@Suppress("ClassName")
class V2__ExtendDataRequest : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_requests " +
                "ADD COLUMN reporting_period varchar(255) NOT NULL DEFAULT '2022', " +
                "ADD COLUMN message_history text, " +
                "ADD COLUMN last_modified_date bigint NOT NULL DEFAULT (extract(epoch from now()) * 1000)::bigint, " +
                "ADD COLUMN request_status varchar(255) NOT NULL DEFAULT 'Open'",
        )
    }
}
