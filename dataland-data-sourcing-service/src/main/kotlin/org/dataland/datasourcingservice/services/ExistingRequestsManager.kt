package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.exceptions.RequestNotFoundApiException
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.DisplayedState
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.ExtendedRequestHistoryEntry
import org.dataland.datasourcingservice.model.request.ExtendedStoredRequest
import org.dataland.datasourcingservice.model.request.RequestHistoryEntry
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.utils.RequestLogger
import org.dataland.datasourcingservice.utils.deleteRepeatingDisplayedStates
import org.dataland.datasourcingservice.utils.getExtendedRequestHistory
import org.dataland.datasourcingservice.utils.getRequestHistory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

/**
 * Service responsible for managing existing (data) requests in the sense of the data sourcing service.
 */
@Service("ExistingRequestsManager")
class ExistingRequestsManager
    @Autowired
    constructor(
        private val requestRepository: RequestRepository,
        private val requestDataSourcingAssigner: RequestDataSourcingAssigner,
        private val dataRevisionRepository: DataRevisionRepository,
        private val dataSourcingServiceMessageSender: DataSourcingServiceMessageSender,
        private val requestQueryManager: RequestQueryManager,
        private val dataSourcingManager: DataSourcingManager,
    ) {
        private val requestLogger = RequestLogger()

        /**
         Retrieves a stored data request by its ID.
         * @param dataRequestId The UUID of the data request to retrieve.
         * @return The StoredRequest object corresponding to the given ID.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional(readOnly = true)
        fun getRequest(dataRequestId: UUID): ExtendedStoredRequest =
            requestRepository
                .findByIdAndFetchDataSourcingEntity(dataRequestId)
                ?.let { requestQueryManager.transformRequestEntityToExtendedStoredRequest(it) }
                ?: throw RequestNotFoundApiException(
                    dataRequestId,
                ).also { requestLogger.logMessageForGettingDataRequest(dataRequestId, UUID.randomUUID()) }

        /**
         * Updates the state of a data request identified by its ID.
         * If the state is changed from Open to Processing, it ensures that a corresponding DataSourcingEntity exists.
         * @param dataRequestId The UUID of the data request to update.
         * @param newRequestState The new state to set for the data request.#
         * @param adminComment Optional comment from the admin regarding the state change.
         * @return The updated StoredRequest object.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional
        fun patchRequestState(
            dataRequestId: UUID,
            newRequestState: RequestState,
            adminComment: String?,
        ): StoredRequest {
            requestLogger.logMessageForPatchingRequestState(dataRequestId, newRequestState)

            val requestEntity =
                requestRepository.findByIdAndFetchDataSourcingEntity(dataRequestId)
                    ?: throw RequestNotFoundApiException(
                        dataRequestId,
                    )
            val oldRequestState = requestEntity.state
            requestEntity.lastModifiedDate = Instant.now().toEpochMilli()
            requestEntity.state = newRequestState

            if (adminComment != null) {
                requestLogger.logMessageForPatchingAdminComment(dataRequestId, adminComment)
                requestEntity.adminComment = adminComment
            }

            if (requestEntity.state == RequestState.Processing) {
                val dataSourcingEntity =
                    requestDataSourcingAssigner.useExistingOrCreateDataSourcingAndAddRequest(requestEntity)
                dataSourcingServiceMessageSender.sendMessageToAccountingServiceOnRequestProcessing(
                    dataSourcingEntity = dataSourcingEntity,
                    requestEntity = requestEntity,
                )
            } else {
                val dataSourcingEntity = requestEntity.dataSourcingEntity
                if (isWithdrawalOfProcessedOrProcessingRequest(newRequestState, oldRequestState) && dataSourcingEntity != null) {
                    dataSourcingServiceMessageSender.sendMessageToAccountingServiceOnRequestWithdrawn(
                        dataSourcingEntity = dataSourcingEntity,
                        requestEntity = requestEntity,
                    )
                }
                requestRepository.save(requestEntity)
            }
            return requestEntity.toStoredDataRequest()
        }

        private fun isWithdrawalOfProcessedOrProcessingRequest(
            newRequestState: RequestState,
            oldRequestState: RequestState,
        ): Boolean =
            newRequestState == RequestState.Withdrawn &&
                oldRequestState in
                listOf(
                    RequestState.Processing,
                    RequestState.Processed,
                )

        /**
         * Updates the priority of a data request identified by its ID.
         * @param dataRequestId The UUID of the data request to update.
         * @param newRequestPriority The new priority to set for the data request.
         * @param adminComment Optional comment from the admin regarding the priority change.
         * @return The updated StoredRequest object.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional
        fun patchRequestPriority(
            dataRequestId: UUID,
            newRequestPriority: RequestPriority,
            adminComment: String?,
        ): StoredRequest {
            requestLogger.logMessageForPatchingRequestPriority(dataRequestId, newRequestPriority)

            val requestEntity =
                requestRepository.findByIdAndFetchDataSourcingEntity(dataRequestId) ?: throw RequestNotFoundApiException(
                    dataRequestId,
                )
            requestEntity.requestPriority = newRequestPriority
            requestEntity.lastModifiedDate = Instant.now().toEpochMilli()

            if (adminComment != null) {
                requestLogger.logMessageForPatchingAdminComment(dataRequestId, adminComment)
                requestEntity.adminComment = adminComment
            }

            return requestEntity.toStoredDataRequest()
        }

        /**
         * A data class representing a combined history entry that includes the timestamp, request state,
         * data sourcing state, displayed state, and admin comment.
         * This class is used to create a unified view of the history of a data request and its associated data
         * sourcing entries.
         */
        data class CombinedHistoryEntryDefault(
            val timestamp: Long,
            val requestState: RequestState?,
            val dataSourcingState: DataSourcingState?,
            val displayedState: DisplayedState?,
            val adminComment: String?,
        )

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
            val dataSourcingID = getRequest(requestId).dataSourcingEntityId
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
            var combinedHistory = getRequestHistory(requestHistory, dataSourcingHistory)
            return deleteRepeatingDisplayedStates(combinedHistory)
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
            return getExtendedRequestHistory(requestHistory, dataSourcingHistory)
        }
    }
