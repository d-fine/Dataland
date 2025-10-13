package org.dataland.datasourcingservice.utils

import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Utility service class for logging messages related to data requests.
 */
class RequestLogger {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Logs an appropriate message when a single data request is created
     */
    fun logMessageForReceivingSingleDataRequest(
        companyId: UUID,
        userId: UUID,
        correlationId: UUID,
    ) {
        logger.info(
            "Received a single data request with Identifier $companyId" +
                " for user $userId. -> Processing it. " +
                "(correlationId: $correlationId)",
        )
    }

    /**
     * Logs an appropriate message when a request is retrieved by its id
     */
    fun logMessageForGettingDataRequest(
        dataRequestId: UUID,
        correlationId: UUID,
    ) {
        logger.info(
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
        logger.info(
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
        logger.info(
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
        logger.info(
            "Patching request $dataRequestId " +
                "with admin comment $adminComment.",
        )
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logMessageForStoringDataRequest(dataRequestId: UUID) {
        logger.info("Stored data request with dataRequestId $dataRequestId.")
    }
}
