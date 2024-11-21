package org.dataland.datalandqaservice.db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the QA review database
 */
@Suppress("ClassName")
class V3__CreateQaReviewTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS qa_review (" +
                "event_id varchar(255) NOT NULL , " +
                "data_id varchar(255) NOT NULL, " +
                "company_id varchar(255) NOT NULL, " +
                "company_name varchar(255) NOT NULL, " +
                "data_type varchar(255) NOT NULL, " +
                "reporting_period varchar(255) NOT NULL, " +
                "timestamp smallint NOT NULL, " +
                "qa_status varchar(255) NOT NULL, " +
                "triggeringUserId varchar(255) NOT NULL, " +
                "comment varchar(255), " +
                "PRIMARY KEY (event_id)" +
                ")",
        )
    }
}
