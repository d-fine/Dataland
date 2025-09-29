package org.dataland.datasourcingservice.utils

import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.services.SingleRequestManager
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Utility service class for logging messages related to data requests.
 */
class RequestLogger {
    private val singleDataRequestLogger = LoggerFactory.getLogger(SingleRequestManager::class.java)

    /**
     * Logs an appropriate message when a single data request by dimensions has happened.
     */
    fun logMessageForReceivingSingleDataRequest(
        companyId: UUID,
        userId: UUID,
        correlationId: UUID,
    ) {
        singleDataRequestLogger.info(
            "Received a single data request with Identifier $companyId" +
                " for user $userId. -> Processing it. " +
                "(correlationId: $correlationId)",
        )
    }

    /**
     * Logs an appropriate message when a single data request by id has happened.
     */
    fun logMessageForGettingSingleDataRequest(
        dataRequestId: UUID,
        correlationId: UUID,
    ) {
        singleDataRequestLogger.info(
            "Received GET request for a single data request with id $dataRequestId" +
                " -> Processing it. " +
                "(correlationId: $correlationId)",
        )
    }

    /**
     * Logs an appropriate message when the state of a data request is updated
     */
    fun logMessageForPatchingRequestState(
        dataRequestId: UUID,
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
        dataRequestId: UUID,
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
        dataRequestId: UUID,
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
        singleDataRequestLogger.error(
            "The following data request already exists for user with id $userId and therefore " +
                "is not being recreated: (companyId: $companyId, framework: $framework, " +
                "reportingPeriod: $reportingPeriod, requestStatus: $requestState)",
        )
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logMessageForStoringDataRequest(dataRequestId: UUID) {
        singleDataRequestLogger.info("Stored data request with dataRequestId $dataRequestId.")
    }
}
