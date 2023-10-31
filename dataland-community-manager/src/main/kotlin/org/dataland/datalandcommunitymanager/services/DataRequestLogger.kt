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

    private val nameOfTargetService = "DATALANAD-DATA-REQUEST-MANAGER"
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun wrapServiceName(logMessageToWrap: String): String {
        return "$nameOfTargetService: $logMessageToWrap"
    }

    /**
     * Logs an appropriate message when a bulk data request has happened.
     */
    fun logMessageForBulkDataRequest(requestingUserId: String, bulkDataRequestId: String) {
        logger.info(
            wrapServiceName(
                "Received a bulk data request by userId $requestingUserId " +
                    "-> Processing it with bulkDataRequestId $bulkDataRequestId",
            ),
        ) // TODO Discuss: Is this ok from data privacy perspective? We also do this in api key manager.
    }

    /**
     * Logs an appropriate message when a user has retrieved all their data requests.
     */
    fun logMessageForRetrievingDataRequestsForUser(requestingUserId: String) {
        logger.info(wrapServiceName("Retrieved data requests for user $requestingUserId"))
    }

    /**
     * Logs an appropriate message when it has been checked if a specific data request already exists and that check
     * returned "true".
     */
    fun logMessageForCheckingIfDataRequestAlreadyExists(
        requestingUserId: String,
        identifierValue: String,
        framework: DataTypeEnum,
    ) {
        logger.info(
            wrapServiceName(
                "The following data request already exists and therefore is not being recreated: " +
                    "(requestingUser: $requestingUserId, identifierValue: $identifierValue, framework: $framework)",
            ),
        )
    }

    /**
     * Logs an appropriate message when a data request has been stored in the database.
     */
    fun logMessageForStoringDataRequest(dataRequestId: String, bulkDataRequestId: String? = null) {
        var logMessage = "Stored data request with dataRequestId $dataRequestId "
        if (bulkDataRequestId != null) {
            logMessage += "while processing a bulk data request with bulkDataRequestId: $bulkDataRequestId"
        }
        logger.info(wrapServiceName(logMessage))
    }
}
