package org.dataland.datalandqaservice.db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Drops legacy table review_information containing the review QA history for older datasets
 */
@Suppress("ClassName")
class V6__RenameDataPointColumns : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE data_point_qa_reports RENAME COLUMN data_id TO data_point_id;" +
                "ALTER TABLE data_point_qa_reports RENAME COLUMN data_point_identifier TO data_point_type;" +
                "ALTER TABLE data_point_qa_review RENAME COLUMN data_id TO data_point_id;" +
                "ALTER TABLE data_point_qa_review RENAME COLUMN data_point_identifier TO data_point_type;",
        )
    }
}
