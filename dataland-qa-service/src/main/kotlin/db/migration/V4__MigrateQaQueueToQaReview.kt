package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.Connection
import java.util.UUID

/**
 * This class migrates the QaService ReviewInformation Table and ReviewQueue Table into a new combined QaReviewEntity Table
 */
@Suppress("ClassName")
class V4__MigrateQaQueueToQaReview : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val targetConnection = context!!.connection
        migrateQueueData(targetConnection)
    }

    /**
     * Migration of qa-queue data into new qa_review table (no data for qa_status or qa_reviewer)
     */
    private fun migrateQueueData(targetConnection: Connection) {
        val queueResultSet =
            targetConnection.createStatement().executeQuery(
                "SELECT" +
                    " data_id," +
                    " reception_time," +
                    " comment," +
                    " company_id," +
                    " company_name," +
                    " framework," +
                    " reporting_period" +
                    " FROM review_queue",
            )

        val queueInsertStatement =
            targetConnection.prepareStatement(
                "INSERT INTO qa_review (event_id, data_id, company_id, company_name, data_type, reporting_period, timestamp," +
                    " qa_status, triggeringUserId, comment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            )

        while (queueResultSet.next()) {
            val eventId = UUID.randomUUID().toString()
            val dataId = queueResultSet.getString("data_id")
            val companyId = queueResultSet.getString("company_id")
            val companyName = queueResultSet.getString("company_name")
            val dataType = queueResultSet.getString("framework")
            val reportingPeriod = queueResultSet.getString("reporting_period")
            val timestamp = queueResultSet.getLong("reception_time")
            val qaStatus = "Pending"
            val triggeringUserId = "Lost in Migration"
            val comment: String? = queueResultSet.getString("comment")

            var index = 1
            queueInsertStatement.setString(index++, eventId)
            queueInsertStatement.setString(index++, dataId)
            queueInsertStatement.setString(index++, companyId)
            queueInsertStatement.setString(index++, companyName)
            queueInsertStatement.setString(index++, dataType)
            queueInsertStatement.setString(index++, reportingPeriod)
            queueInsertStatement.setLong(index++, timestamp)
            queueInsertStatement.setString(index++, qaStatus)
            queueInsertStatement.setString(index++, triggeringUserId)
            queueInsertStatement.setString(index, comment)
        }

        queueResultSet.close()
        queueInsertStatement.close()
    }
}
