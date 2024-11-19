package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.Connection
import java.sql.PreparedStatement

/**
 * This class migrates the QaService ReviewInformation Table and ReviewQueue Table into a new combined QaReviewEntity Table
 */

@Suppress("ClassName")
class V3__CombineQaReviewInfoAndQueue : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        // waiting two minutes before running script to ensure backend is running
        val waitingTimeMs: Long = 120000L
        Thread.sleep(waitingTimeMs)
        val connection = context!!.connection

        migrateQueueData(connection)
        migrateInformationData(connection)
    }

    /**
     * Migration of qa-queue data into new qa_review table (no data for qa_status or qa_reviewer)
     */
    private fun migrateQueueData(connection: Connection) {
        val queueResultSet =
            connection.createStatement().executeQuery(
                "SELECT data_id, reception_time, comment, company_id, company_name, framework, reporting_period FROM review_queue",
            )

        val queueInsertStatement =
            connection.prepareStatement(
                "INSERT INTO qa_review (data_id, company_id, company_name, data_type, reporting_period, timestamp, comment)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?)",
            )

        while (queueResultSet.next()) {
            val dataId = queueResultSet.getString("data_id")
            val timeStamp = queueResultSet.getLong("reception_time")
            val comment: String? = queueResultSet.getString("comment")
            val companyId = queueResultSet.getString("company_id")
            val companyName = queueResultSet.getString("company_name")
            val dataType = queueResultSet.getString("framework")
            val reportingPeriod = queueResultSet.getString("reporting_period")

            val queueData =
                Data(
                    dataId = dataId,
                    companyId = companyId,
                    companyName = companyName,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    timeStamp = timeStamp,
                    comment = comment,
                    commentIndex = 7,
                )
            insertData(queueInsertStatement, queueData)
        }

        queueResultSet.close()
        queueInsertStatement.close()
    }

    /**
     * Migration of qa-information data into new qa_review table (missing data is added from meta_data in backend)
     */
    private fun migrateInformationData(connection: Connection) {
        val informationResultSet =
            connection.createStatement().executeQuery(
                "SELECT data_id, qa_status, reception_time, reviewer_keycloak_id, message FROM review_information",
            )

        val informationInsertStatement =
            connection.prepareStatement(
                "INSERT INTO qa_review (data_id, company_id, company_name, data_type,reporting_period, timestamp," +
                    " qa_status, reviewer_id, comment) Values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            )

        while (informationResultSet.next()) {
            val dataId = informationResultSet.getString("data_id")
            val qaStatus = informationResultSet.getString("qa_status")
            val timeStamp = informationResultSet.getLong("reception_time")
            val reviewerId = informationResultSet.getString("reviewer_keycloak_id")
            val comment: String? = informationResultSet.getString("message")

            // Get missing data for company Id, data type and reporting period form meta data tabel
            val missingDataQuery =
                "SELECT data_type, reporting_period, company_id FROM data_meta_information WHERE data_id = ?"
            val preparedMetaData = connection.prepareStatement(missingDataQuery)

            preparedMetaData.setString(1, dataId)
            val metaDataResultSet = preparedMetaData.executeQuery()

            var dataType = ""
            var reportingPeriod = ""
            var companyId = ""
            var companyName = ""

            if (metaDataResultSet.next()) {
                dataType = metaDataResultSet.getString("data_type")
                reportingPeriod = metaDataResultSet.getString("reporting_period")
                companyId = metaDataResultSet.getString("company_id")
            }

            // get missing data for company name from stored companies table
            val companyNameQuery = "SELECT company_name FROM stored_companies WHERE company_id = ?"
            val preparedCompanyStatement = connection.prepareStatement(companyNameQuery)

            preparedCompanyStatement.setString(1, companyId)
            val companyResultSet = preparedCompanyStatement.executeQuery()

            if (companyResultSet.next()) {
                companyName = companyResultSet.getString("company_name")
            }

            val informationData =
                Data(
                    dataId = dataId,
                    companyId = companyId,
                    companyName = companyName,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    timeStamp = timeStamp,
                    qaStatus = qaStatus,
                    reviewerId = reviewerId,
                    comment = comment,
                )
            insertData(informationInsertStatement, informationData)

            companyResultSet.close()
            preparedCompanyStatement.close()
            metaDataResultSet.close()
            preparedMetaData.close()
        }
        informationInsertStatement.close()
        informationResultSet.close()
    }

    /**
     * inserts Data from the db tables into the prepareStatement and stores them into the new table
     */
    private fun insertData(
        statement: PreparedStatement,
        dataObject: Data,
    ) {
        statement.setString(dataObject.dataIdIndex, dataObject.dataId)
        statement.setString(dataObject.companyIdIndex, dataObject.companyId)
        statement.setString(dataObject.companyNameIndex, dataObject.companyName)
        statement.setString(dataObject.dataTypeIndex, dataObject.dataType)
        statement.setString(dataObject.reportingPeriodIndex, dataObject.reportingPeriod)
        statement.setLong(dataObject.timeStampIndex, dataObject.timeStamp)
        statement.setString(dataObject.commentIndex, dataObject.comment)

        if (dataObject.qaStatus != "") {
            statement.setString(dataObject.qaStatusIndex, dataObject.qaStatus)
        }
        if (dataObject.reviewerId != "") {
            statement.setString(dataObject.reviewerIdIndex, dataObject.reviewerId)
        }
        statement.executeUpdate()
    }
}

/**
 * Data Object to store the retrieved data
 */
data class Data(
    val dataId: String,
    val companyId: String,
    val companyName: String,
    val dataType: String,
    val reportingPeriod: String,
    val timeStamp: Long,
    val qaStatus: String = "",
    val reviewerId: String = "",
    val comment: String?,
    // indices of values in the insertStatements
    val dataIdIndex: Int = 1,
    val companyIdIndex: Int = 2,
    val companyNameIndex: Int = 3,
    val dataTypeIndex: Int = 4,
    val reportingPeriodIndex: Int = 5,
    val timeStampIndex: Int = 6,
    val qaStatusIndex: Int = 7,
    val reviewerIdIndex: Int = 8,
    val commentIndex: Int = 9,
)
