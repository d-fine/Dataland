package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.request.ExtendedRequestHistoryEntryData
import org.dataland.datasourcingservice.model.request.RequestHistoryEntryData
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.utils.RequestAndDataSourcingHistory
import org.dataland.datasourcingservice.utils.RequestStateHistoryUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service responsible for retrieving the history of data requests and their associated data sourcing entries.
 */
@Service("RequestHistoryService")
class RequestHistoryService
    @Autowired
    constructor(
        private val dataRevisionRepository: DataRevisionRepository,
        private val dataSourcingManager: DataSourcingManager,
        private val existingRequestsManager: ExistingRequestsManager,
    ) {
        /**
         * Retrieves the request and data sourcing history for a specific data request (identified by its ID),
         * sorted by modification date.
         *
         * @param requestId The UUID string of the data request whose historys are to be retrieved.
         * @return A RequestAndDataSourcingHistory object containing the list of RequestEntity revisions and the list
         * of DataSourcingWithoutReferences revisions associated with the specified data request.
         */
        @Transactional(readOnly = true)
        fun retrieveStateHistoryByRequestId(requestId: UUID): RequestAndDataSourcingHistory {
            val requestHistory = dataRevisionRepository.listDataRequestRevisionsById(requestId)
            val dataSourcingID = existingRequestsManager.getRequest(requestId).dataSourcingEntityId
            var dataSourcingHistory = emptyList<DataSourcingWithoutReferences>()
            if (dataSourcingID != null) {
                dataSourcingHistory =
                    dataSourcingManager.retrieveDataSourcingHistory(ValidationUtils.convertToUUID(dataSourcingID), true)
            }
            return RequestAndDataSourcingHistory(
                requestHistory.sortedBy { it.lastModifiedDate },
                dataSourcingHistory.sortedBy { it.lastModifiedDate },
            )
        }

        /**
         * Retrieves the combined history of request and data sourcing state changes for a specific data request
         * (identified by its ID), intended for use in the frontend non-admin view and sorted by modification date.
         *
         * @param requestId The UUID string of the data request whose combined history is to be retrieved.
         * @return A list of RequestHistoryEntryData objects representing the combined revision state history
         * of the specified data request and its associated data sourcing entries.
         */
        @Transactional(readOnly = true)
        fun retrieveRequestHistory(requestId: UUID): List<RequestHistoryEntryData> =
            RequestStateHistoryUtils.getRequestHistory(
                retrieveStateHistoryByRequestId(requestId),
            )

        /**
         * Retrieves the combined history of request and data sourcing state changes for a specific data request
         * (identified by its ID), intended for use in the frontend admin view and sorted by modification date.
         *
         * @param requestId The UUID string of the data request whose combined history is to be retrieved.
         * @return A list of ExtendedRequestHistoryEntryData objects representing the combined revision state history
         * of the specified data request and its associated data sourcing entries.
         */
        @Transactional(readOnly = true)
        fun retrieveExtendedRequestHistory(requestId: UUID): List<ExtendedRequestHistoryEntryData> =
            RequestStateHistoryUtils.getExtendedRequestHistory(
                retrieveStateHistoryByRequestId(requestId),
            )
    }
