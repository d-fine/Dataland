package org.dataland.datalandqaservice.db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * Change column names to new version
 */
@Suppress("ClassName")
class V6__RenameDataPoints : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger("Migration V6")
    override fun migrate(context: Context?) {
        logger.info("Renaming data point columns")
        val connection = context!!.connection
        val resultSetReports = connection.metaData.getTables(null, null, "data_point_qa_reports", null)

        if (resultSetReports.next()) {
            logger.info("Found table data_point_qa_reports")
            connection.createStatement().execute(
                "ALTER TABLE data_point_qa_reports RENAME COLUMN data_id TO data_point_id;" +
                    "ALTER TABLE data_point_qa_reports RENAME COLUMN data_point_identifier TO data_point_type;",
            )
        }
        val resultSetReview = connection.metaData.getTables(null, null, "data_point_qa_review", null)
        if (resultSetReview.next()) {
            logger.info("Found table data_point_qa_review")
            connection.createStatement().execute(
                "ALTER TABLE data_point_qa_review RENAME COLUMN data_id TO data_point_id;" +
                    "ALTER TABLE data_point_qa_review RENAME COLUMN data_point_identifier TO data_point_type;",
            )
        }
    }
}
