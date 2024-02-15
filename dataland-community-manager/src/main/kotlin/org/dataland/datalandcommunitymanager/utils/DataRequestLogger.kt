package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.BulkDataRequestManager
import org.dataland.datalandcommunitymanager.services.CauseOfMail
import org.dataland.datalandcommunitymanager.services.SingleDataRequestManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Implementation of a request manager service for all operations concerning the processing of data requests
 */
@Service("DataRequestLogger")
class DataRequestLogger {

    private val bulkDataRequestLogger = LoggerFactory.getLogger(BulkDataRequestManager::class.java)
    private val singleDataRequestLogger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Logs an appropriate message when a bulk data request has happened.
     */
    fun logMessageForBulkDataRequest(bulkDataRequestId: String) {
        bulkDataRequestLogger.info(
            "Received a bulk data request from a user. " +
                "-> Processing it with bulkDataRequestId $bulkDataRequestId",

        )
    }

    /**
     * Logs an appropriate message when a bulk data request has happened.
     */
    fun logMessageForSingleDataRequestReceived() {
        singleDataRequestLogger.info("Received a single data request from a user.")
    }

    /**
     * Logs an appropriate message when a single data request has happened.
     */
    fun logMessageForReceivingSingleDataRequest(companyIdentifier: String) {
        bulkDataRequestLogger.info(
            "Received a single data request with companyIdentifier $companyIdentifier by a user. " +
                "-> Processing it",
        )
    }

    /**
     * Logs an appropriate message when a bulk data request email is sent.
     */
    fun logMessageForSendBulkDataRequestEmail(bulkDataRequestId: String) {
        bulkDataRequestLogger.info(
            "Sending email after ${CauseOfMail.BulkDataRequest}" +
                " with bulkDataRequestId $bulkDataRequestId has been processed",

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
        identifierValue: String,
        framework: DataTypeEnum,
    ) {
        bulkDataRequestLogger.info(
            "The following data request already exists for the requesting user and therefore " +
                "is not being recreated: (identifierValue: $identifierValue, framework: $framework)",
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
        logMessage += if (companyId == null) {
            "is currently not associated with a company that exists on Dataland."
        } else {
            "can be associated with the companyId $companyId on Dataland."
        }
        bulkDataRequestLogger.info(logMessage)
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logMessageForStoringDataRequest(dataRequestId: String) {
        bulkDataRequestLogger.info("Stored data request with dataRequestId $dataRequestId")
    }

    /**
     * Logs an appropriate message when the status of a data request is updated
     */
    fun logMessageForPatchingRequestStatus(dataRequestId: String, requestStatus: RequestStatus) {
        singleDataRequestLogger.info("Patching request $dataRequestId with status $requestStatus")
    }
}
