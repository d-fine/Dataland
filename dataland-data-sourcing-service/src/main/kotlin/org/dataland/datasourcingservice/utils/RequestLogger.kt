package org.dataland.datasourcingservice.utils

import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.PreprocessedRequest
import org.dataland.datasourcingservice.services.BulkRequestManager
import org.dataland.datasourcingservice.services.SingleRequestManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Utility service class for logging messages related to data requests.
 */
@Service("RequestLogger")
class RequestLogger {
    private val singleDataRequestLogger = LoggerFactory.getLogger(SingleRequestManager::class.java)
    private val bulkDataRequestLogger = LoggerFactory.getLogger(BulkRequestManager::class.java)

    /**
     * Logs an appropriate message when a single data request has happened.
     */
    fun logMessageForReceivingSingleDataRequest(preprocessedRequest: PreprocessedRequest) {
        singleDataRequestLogger.info(
            "Received a single data request with Identifier ${preprocessedRequest.companyId}" +
                " for user ${preprocessedRequest.userId}. -> Processing it. " +
                "(correlationId: ${preprocessedRequest.correlationId})",
        )
    }

    /**
     * Logs an appropriate message when the state of a data request is updated
     */
    fun logMessageForPatchingRequestState(
        dataRequestId: String,
        requestState: RequestState,
    ) {
        singleDataRequestLogger.info(
            "Patching request $dataRequestId with " +
                "request status $requestState.",
        )
    }

    /**
     * Logs an appropriate message when the data request priority is updated
     */
    fun logMessageForPatchingRequestPriority(
        dataRequestId: String,
        requestPriority: RequestPriority,
    ) {
        singleDataRequestLogger.info(
            "Patching request $dataRequestId " +
                "with request priority $requestPriority.",
        )
    }

    /**
     * Logs an appropriate message when the admin comment of the request priority is updated
     */
    fun logMessageForPatchingAdminComment(
        dataRequestId: String,
        adminComment: String,
    ) {
        singleDataRequestLogger.info(
            "Patching request $dataRequestId " +
                "with admin comment $adminComment.",
        )
    }

    /**
     * Logs an appropriate message when it has been checked if a specific data request already exists and that check
     * returned "true".
     */
    fun logMessageForCheckingIfDataRequestAlreadyExists(
        userId: UUID,
        companyId: UUID,
        framework: String,
        reportingPeriod: String,
        requestState: RequestState,
    ) {
        bulkDataRequestLogger.info(
            "The following data request already exists for user with id $userId and therefore " +
                "is not being recreated: (companyId: $companyId, framework: $framework, " +
                "reportingPeriod: $reportingPeriod, requestStatus: $requestState)",
        )
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logMessageForStoringDataRequest(dataRequestId: UUID) {
        bulkDataRequestLogger.info("Stored data request with dataRequestId $dataRequestId.")
    }
}
