package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.springframework.stereotype.Component
import java.util.*

@Component("QaLogMessageBuilder")
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
     * Generates a message to inform that a request was received to return a QA report by its ID
     * @param dataId The ID of the dataset for which the report shall be returned
     * @param qaReportId The ID of the report that shall be returned
     * @returns the message to log
     */
    fun getQaReportMessage(qaReportId: String, dataId: String): String {
        return "Received a request to get QA report information with dataId '$dataId' and qaReportId '$qaReportId'. "
    }

    /**
     * Generates a message to inform that a QA report has been successfully returned
     * @param dataId The ID of the dataset for which the report shall be returned
     * @param qaReportId The ID of the report that shall be returned
     * @param correlationId The correlation ID in association with this operation
     * @returns the message to log
     */
    fun getQaReportSuccessMessage(qaReportId: String, dataId: String, correlationId: String): String {
        return "Received QA report with qaReportId '$qaReportId' for dataId '$dataId' " +
            "Correlation ID '$correlationId'"
    }
}
