package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.springframework.stereotype.Component

/**
 * A builder for log messages related to QA reports.
 */
@Component
class QaLogMessageBuilder {

    /**
     * Generates a message to inform that a QA report for a specific dataset shall be posted
     * @param reporterUserId The ID of the user who requests the post
     * @param dataId The ID of the dataset for which a QA report shall be posted
     * @returns the message to log
     */
    fun postQaReportMessage(
        dataId: String,
        reporterUserId: String,
    ): String {
        return "Received a request from user '$reporterUserId' to post a QA report for data ID '$dataId'"
    }

    /**
     * Generates a message to inform that a request was received to return a QA report by its ID
     * @param dataId The ID of the dataset for which the report shall be returned
     * @param qaReportId The ID of the report that shall be returned
     * @returns the message to log
     */
    fun getQaReportMessage(qaReportId: String, dataId: String): String {
        return "Received a request to get QA report information with dataId '$dataId' and qaReportId '$qaReportId'. "
    }

    /**
     * Generates a message to inform that a request was received to return a QA report by its data id
     * @param dataId The ID of the dataset for which the report shall be returned
     * @returns the message to log
     */
    fun getAllQaReportsForDataIdMessage(dataId: String, reviewerId: String?): String {
        return "Received a request to get all QA report information for dataId '$dataId' " +
            "(and reviewer id: $reviewerId)."
    }

    /**
     * Generates a message to inform that a request was received to mark a QA report as active or inactive
     * @param qaReportId The ID of the report that shall be marked as inactive
     * @param dataId The ID of the dataset for which the report shall be marked as inactive
     * @param active The status the report shall be marked with
     * @returns the message to log
     */
    fun requestChangeQaReportStatus(qaReportId: String, dataId: String, active: Boolean): String {
        return "Received a request to mark QA report with ID '$qaReportId' for data ID '$dataId' as active=$active."
    }
}
