package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.BulkDataRequestResponse
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.BulkDataRequestManager
import org.dataland.datalandcommunitymanager.services.SingleDataRequestManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Implementation of a request manager service for all operations concerning the processing of data requests
 */
@Suppress("TooManyFunctions")
@Service("DataRequestLogger")
class DataRequestLogger {
    private val bulkDataRequestLogger = LoggerFactory.getLogger(BulkDataRequestManager::class.java)
    private val singleDataRequestLogger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Logs an appropriate message when a bulk data request has happened.
     */
    fun logMessageForBulkDataRequest(correlationId: String) {
        bulkDataRequestLogger.info(
            "Received a bulk data request from a user. " +
                "-> Processing it with correlationId: $correlationId",
        )
    }

    /**
     * Logs an appropriate message when a single data request has happened.
     */
    fun logMessageForReceivingSingleDataRequest(preprocessedRequest: SingleDataRequestManager.PreprocessedRequest) {
        singleDataRequestLogger.info(
            "Received a single data request with Identifier ${preprocessedRequest.companyId}" +
                " for user ${preprocessedRequest.userId}. -> Processing it. " +
                "(correlationId: ${preprocessedRequest.correlationId})",
        )
    }

    /**
     * Logs an appropriate message when a bulk data request email sending is initiated.
     */
    fun logMessageForSendBulkDataRequestEmailMessage(correlationId: String) {
        bulkDataRequestLogger.info(
            "Notifying email sender that an email should be sent after BulkDataRequest" +
                " with correlationId: $correlationId has been processed",
        )
    }

    /**
     * Logs an appropriate message when a user has retrieved all their data requests.
     */
    fun logMessageForRetrievingDataRequestsForUser() {
        bulkDataRequestLogger.info("A user has retrieved all their data requests.")
    }

    /**
     * Logs an appropriate message when it has been checked if a specific data request already exists and that check
     * returned "true".
     */
    fun logMessageForCheckingIfDataRequestAlreadyExists(
        userId: String,
        companyId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
        requestStatus: RequestStatus,
    ) {
        bulkDataRequestLogger.info(
            "The following data request already exists for user with id $userId and therefore " +
                "is not being recreated: (companyId: $companyId, framework: $framework, " +
                "reportingPeriod: $reportingPeriod, requestStatus: $requestStatus)",
        )
    }

    /**
     * Logs an appropriate message when it has been checked if an identifier value can be cross-referenced with a
     * companyId that already exists on Dataland.
     */
    fun logMessageWhenCrossReferencingIdentifierValueWithDatalandCompanyId(
        identifierValue: String,
        companyId: String?,
    ) {
        var logMessage = "The identifier value $identifierValue "
        logMessage +=
            if (companyId == null) {
                "is currently not associated with a company that exists on Dataland."
            } else {
                "can be associated with the companyId $companyId on Dataland."
            }
        bulkDataRequestLogger.info(logMessage)
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logBulkDataOverwiew(
        response: BulkDataRequestResponse,
        correlationId: String,
    ) {
        bulkDataRequestLogger.info("Processed bulk data request with correlationId: $correlationId.")
        bulkDataRequestLogger.info("Number of accepted requests: ${response.acceptedDataRequests.size}")
        bulkDataRequestLogger.info("Number of already existing non-final requests: ${response.alreadyExistingNonFinalRequests.size}")
        bulkDataRequestLogger.info("Number of already existing datasets: ${response.alreadyExistingDatasets.size}")
        bulkDataRequestLogger.info("Number of rejected identifiers: ${response.rejectedCompanyIdentifiers.size}")
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logMessageForStoringDataRequest(dataRequestId: String) {
        bulkDataRequestLogger.info("Stored data request with dataRequestId $dataRequestId.")
    }

    /**
     * Logs an appropriate message when the status of a data request is updated
     */
    fun logMessageForPatchingRequestStatusOrAccessStatus(
        dataRequestId: String,
        requestStatus: RequestStatus,
        accessStatus: AccessStatus?,
    ) {
        singleDataRequestLogger.info(
            "Patching request $dataRequestId with " +
                "request status $requestStatus and access status $accessStatus.",
        )
    }

    /**
     * Logs an appropriate message when the message of a data request is updated
     */
    fun logMessageForPatchingRequestMessage(dataRequestId: String) {
        singleDataRequestLogger.info(
            "Patching request $dataRequestId " +
                "with new message.",
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
}
