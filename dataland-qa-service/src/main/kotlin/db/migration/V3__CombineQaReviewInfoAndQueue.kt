package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This class migrates the QaService ReviewInformation Table and ReviewQueue Table into a new combined QaReviewEntity Table
 */

@Suppress("ClassName")
class V3__CombineQaReviewInfoAndQueue : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val connection = context!!.connection

        /*
         * Migration of qa-queue data into new qa_review table (no data for qa_status or qa_reviewer)
         */
        val queueResultSet =
            connection.createStatement().executeQuery(
                "SELECT data_id, reception_time, comment, company_id, company_name, framework, reporting_period FROM review_queue",
            )

        val queueInsertStatement =
            connection.prepareStatement(
                "INSERT INTO qa_review (data_id, company_id, company_name, data_type, reporting_period, timestamp, comment) VALUES (?, ?, ?, ?, ?, ?, ?)",
            )

        while (queueResultSet.next()) {
            val dataId = queueResultSet.getString("data_id")
            val timestamp = queueResultSet.getLong("reception_time")
            val comment: String? = queueResultSet.getString("comment")
            val companyId = queueResultSet.getString("company_id")
            val companyName = queueResultSet.getString("company_name")
            val dataType = queueResultSet.getString("framework")
            val reportingPeriod = queueResultSet.getString("reporting_period")

            queueInsertStatement.setString(1, dataId)
            queueInsertStatement.setString(2, companyId)
            queueInsertStatement.setString(3, companyName)
            queueInsertStatement.setString(4, dataType)
            queueInsertStatement.setString(5, reportingPeriod)
            queueInsertStatement.setLong(6, timestamp)
            queueInsertStatement.setString(7, comment)

            queueInsertStatement.executeUpdate()
        }

        queueResultSet.close()
        queueInsertStatement.close()

        /*
         * Migration of qa-information data into new qa_review table (missing data is added from meta_data in backend)
         */
        val informationResultSet =
            connection.createStatement().executeQuery(
                "SELECT data_id, qa_status, reception_time, reviewer_keycloak_id, message FROM review_information",
            )

        val informationInsertStatement =
            connection.prepareStatement(
                "INSERT INTO qa_review (data_id, company_id, company_name, data_type, reporting_period, timestamp, qa_status, reviewer_id, comment) Values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
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

            informationInsertStatement.setString(1, dataId)
            informationInsertStatement.setString(2, companyId)
            informationInsertStatement.setString(3, companyName)
            informationInsertStatement.setString(4, dataType)
            informationInsertStatement.setString(5, reportingPeriod)
            informationInsertStatement.setLong(6, timeStamp)
            informationInsertStatement.setString(7, qaStatus)
            informationInsertStatement.setString(8, reviewerId)
            informationInsertStatement.setString(9, comment)

            informationInsertStatement.executeUpdate()

            companyResultSet.close()
            preparedCompanyStatement.close()
            metaDataResultSet.close()
            preparedMetaData.close()
        }

        informationInsertStatement.close()
        informationResultSet.close()
    }
}
