package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.request.ExtendedRequestHistoryEntry
import org.dataland.datasourcingservice.model.request.RequestHistoryEntry
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
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
         * Retrieves the combined history of revisions for a specific data request identified by its ID.
         *
         * @param requestId The UUID string of the data request whose history is to be retrieved.
         * @return A list of CombinedHistoryEntryDefault objects representing the combined revision history
         * of the specified data request and its associated data sourcing entries.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveStateHistoryByRequestId(requestId: UUID): Pair<List<Pair<RequestEntity, Long>>, List<DataSourcingWithoutReferences>> {
            val requestHistory = dataRevisionRepository.listDataRequestRevisionsById(requestId)
            val dataSourcingID = existingRequestsManager.getRequest(requestId).dataSourcingEntityId
            var dataSourcingHistory = emptyList<DataSourcingWithoutReferences>()
            if (dataSourcingID != null) {
                dataSourcingHistory =
                    dataSourcingManager.retrieveDataSourcingHistory(ValidationUtils.convertToUUID(dataSourcingID), true)
            }

            return Pair(requestHistory, dataSourcingHistory)
        }

        /**
         * Retrieves the history of revisions for a specific data request identified by its ID.
         *
         * @param requestId The UUID string of the data request whose history is to be retrieved.
         * @return A list of StoredRequest objects representing the revision history of the specified data request.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveRequestHistory(requestId: UUID): List<RequestHistoryEntry> {
            val (requestHistory, dataSourcingHistory) = retrieveStateHistoryByRequestId(requestId)
            return RequestStateHistoryUtils.getRequestHistory(requestHistory, dataSourcingHistory)
        }

        /**
         * Retrieves the history of revisions for a specific data request identified by its ID.
         *
         * @param requestId The UUID string of the data request whose history is to be retrieved.
         * @return A list of StoredRequest objects representing the revision history of the specified data request.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveExtendedRequestHistory(requestId: UUID): List<ExtendedRequestHistoryEntry> {
            val (requestHistory, dataSourcingHistory) = retrieveStateHistoryByRequestId(requestId)
            return RequestStateHistoryUtils.getExtendedRequestHistory(requestHistory, dataSourcingHistory)
        }
    }
