package org.dataland.datalandcommunitymanager.utils

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.services.SingleDataRequestManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Implementation of a request manager service for all operations concerning the processing of data requests
 */
@Service("DataRequestLogger")
class AccessRequestLogger {

    private val singleDataRequestLogger = LoggerFactory.getLogger(SingleDataRequestManager::class.java)

    /**
     * Logs an appropriate message when it has been checked if a specific data request already exists and that check
     * returned "true".
     */
    fun logMessageForCheckingIfUserHasAccessToDataset(
        companyId: String,
        framework: DataTypeEnum,
        reportingPeriod: String,
    ) {
        singleDataRequestLogger.info(
            "Access for the dataset (companyId: $companyId, framework: $framework, " +
                "reportingPeriod: $reportingPeriod) is granted",
        )
    }

    /**
     * Logs an appropriate message when the status of a data request is updated
     */
    fun logMessageForPatchingAccessStatus(dataRequestId: String, accessStatus: AccessStatus) {
        singleDataRequestLogger.info("Patching request $dataRequestId with status $accessStatus")
    }
}
