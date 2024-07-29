package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.springframework.stereotype.Component
import java.util.*

/**
 * A builder for log messages related to QA reports.
 */
@Component
class QaLogMessageBuilder {

    /**
     * Generates a message to inform that a correlationId has generated been for an operation and potentially
     * logs the qaReportId and/or dataId in association with this operation to improve traceability.
     * @param correlationId that has been generated
     * @param qaReportId associated with the operation
     * @param dataId associated with the operation
     * @returns the message to log
     */
    fun generateCorrelationIdMessage(correlationId: String, qaReportId: String?, dataId: String?): String {
        val parts = mutableListOf<String>()

        dataId?.let { parts.add("dataId: $it") }
        qaReportId?.let { parts.add("qaReportId: $it") }

        val idParts = parts.joinToString(" and ")

        return "Generated correlationId '$correlationId' for an operation associated with $idParts."
    }

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
        return "Received a request from user '$reporterUserId' to post a QA report " +
            "for data ID '$dataId'"
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
}
