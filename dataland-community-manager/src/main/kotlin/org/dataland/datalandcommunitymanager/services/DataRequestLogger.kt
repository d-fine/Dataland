package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * Implementation of a request manager service for all operations concerning the processing of data requests
 */
@Service("DataRequestLogger")
class DataRequestLogger {

    private val logger = LoggerFactory.getLogger(DataRequestManager::class.java)

    /**
     * Logs an appropriate message when a bulk data request has happened.
     */
    fun logMessageForBulkDataRequest(bulkDataRequestId: String) {
        logger.info(
            "Received a bulk data request by a user. " +
                "-> Processing it with bulkDataRequestId $bulkDataRequestId",

        )
    }

    /**
     * Logs an appropriate message when a user has retrieved all their data requests.
     */
    fun logMessageForRetrievingDataRequestsForUser() {
        logger.info("A user has retrieved all their data requests.")
    }

    /**
     * Logs an appropriate message when it has been checked if a specific data request already exists and that check
     * returned "true".
     */
    fun logMessageForCheckingIfDataRequestAlreadyExists(
        identifierValue: String,
        framework: DataTypeEnum,
    ) {
        logger.info(
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
        logger.info(logMessage)
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logMessageForStoringDataRequest(dataRequestId: String, bulkDataRequestId: String? = null) {
        var logMessage = "Stored data request with dataRequestId $dataRequestId "
        if (bulkDataRequestId != null) {
            logMessage += "while processing a bulk data request with bulkDataRequestId: $bulkDataRequestId"
        }
        logger.info(logMessage)
    }
}
