package org.dataland.datalandqaservice.db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script creates an index for a faster search for data point QA udpates
 */
@Suppress("ClassName")
class V8__CreateQaDataPointIndices : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE INDEX data_point_id_idx ON data_point_qa_review (data_point_id);" +
                "CREATE INDEX company_id_idx ON data_point_qa_review (company_id);" +
                "CREATE INDEX qa_status_idx ON data_point_qa_review (qa_status);",
        )
    }
}
