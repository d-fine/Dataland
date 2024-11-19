package org.dataland.datalandqaservice.db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

@Suppress("ClassName")
class V3__CreateQaReviewTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE qa_review (" +
                "data_id varchar(255) NOT NULL, " +
                "company_id varchar(255) NOT NULL, " +
                "compnay_name varchar(255) NOT NULL, " +
                "data_type varchar(255) NOT NULL, " +
                "reporting_period varchar(255) NOT NULL, " +
                "timestamp smallint NOT NULL, " +
                "qa_status varchar(255) NOT NULL, " +
                "reviewer_id varchar(255) NOT NULL, " +
                "comment varchar(255), " +
                "PRIMARY KEY (data_id)" +
                ")",
        )
    }
}
