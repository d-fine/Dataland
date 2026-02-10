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
import org.dataland.datasourcingservice.model.request.BasicStateHistoryEntry
import org.dataland.datasourcingservice.model.request.BasicStateHistoryEntryDefault
import org.dataland.datasourcingservice.model.request.ExtendedStoredRequest
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.utils.RequestLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import kotlin.collections.ifEmpty

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
         * Retrieves the history of revisions for a specific data request identified by its ID.
         * @param requestId The UUID string of the data request whose history is to be retrieved.
         * @return A list of StoredRequest objects representing the revision history of the specified data request.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveRequestHistory(requestId: UUID): List<StoredRequest> =
            dataRevisionRepository
                .listDataRequestRevisionsById(requestId)
                .map { (entity, _) -> entity.toStoredDataRequest() }
                .ifEmpty {
                    throw RequestNotFoundApiException(requestId)
                }

        /**
         * Determines the displayed state based on the request state and data sourcing state.
         * The displayed state is a user-friendly representation of the current status of the request,
         * taking into account both the request state and the data sourcing state.
         *
         * @param requestState - The current state of the request.
         * @param dataSourcingState - The current state of the data sourcing process, which can be null if not applicable.
         * @returns The calculated displayed state for the given request and data sourcing states.
         */
        fun getDisplayedState(
            requestState: RequestState,
            dataSourcingState: DataSourcingState?,
        ): DisplayedState =
            when {
                requestState == RequestState.Withdrawn -> DisplayedState.Withdrawn
                requestState == RequestState.Open -> DisplayedState.Open
                dataSourcingState == null -> DisplayedState.Open
                else ->
                    when (dataSourcingState) {
                        DataSourcingState.Initialized -> DisplayedState.Validated
                        DataSourcingState.DocumentSourcing -> DisplayedState.DocumentSourcing
                        DataSourcingState.DocumentSourcingDone -> DisplayedState.DocumentVerification
                        DataSourcingState.DataExtraction -> DisplayedState.DataExtraction
                        DataSourcingState.DataVerification -> DisplayedState.DataVerification
                        DataSourcingState.NonSourceable -> DisplayedState.NonSourceable
                        DataSourcingState.Done -> DisplayedState.Done
                    }
            }

        /**
         * Filters out entries that are in a transient state, such as 'Processing' without a data sourcing state
         * or 'DataVerification' with a 'Processed' request state. These states are not meaningful for display
         * and can be confusing to users.
         *
         * @param entries - The combined history entries to be filtered.
         * @returns The filtered combined history entries.
         */
        fun filterHistory(entries: List<CombinedHistoryEntryDefault>): List<CombinedHistoryEntryDefault> =
            entries.filter { entry ->
                !(
                    (entry.dataSourcingState == null && entry.requestState == RequestState.Processing) ||
                        (
                            entry.dataSourcingState == DataSourcingState.DataVerification &&
                                entry.requestState == RequestState.Processed
                        )
                )
            }

        /**
         * Fills in missing requestState and dataSourcingState values in the combined history entries.
         * It iterates through the entries and uses the last known states to fill in any gaps.
         * Using both the request state and data sourcing state, it also calculates the displayed state for each entry.
         * The admin comment is also filled in based on the last admin comment from a request state.
         *
         * @param entries - The combined history entries to be processed.
         * @returns The combined history entries with filled gaps.
         */
        fun fillHistoryGaps(entries: List<CombinedHistoryEntryDefault>): List<CombinedHistoryEntryDefault> {
            var lastRequestState: RequestState = RequestState.Open
            var lastDataSourcingState: DataSourcingState? = null
            var lastAdminComment: String? = null
            val result = mutableListOf<CombinedHistoryEntryDefault>()
            for (entry in entries) {
                val currentRequestState = entry.requestState ?: lastRequestState
                val currentDataSourcingState = entry.dataSourcingState ?: lastDataSourcingState
                val currentAdminComment = entry.adminComment ?: lastAdminComment
                result.add(
                    entry.copy(
                        requestState = currentRequestState,
                        dataSourcingState = currentDataSourcingState,
                        adminComment = currentAdminComment,
                        displayedState = getDisplayedState(currentRequestState, currentDataSourcingState),
                    ),
                )
                lastRequestState = currentRequestState
                lastDataSourcingState = currentDataSourcingState
                lastAdminComment = currentAdminComment
            }
            return result
        }

        /**
         * Builds a combined history of request states and data sourcing states based on the provided data request
         * revisions and data sourcing history.
         * It creates a unified list of history entries that include both request state changes and data sourcing state
         * changes, sorted by timestamp.
         * The method also checks that the first entry in the combined history has the request state 'Open', as this is
         * expected for all requests.
         *
         * @param dataRequestRevisions - A list of pairs containing RequestEntity objects and their corresponding
         * revision numbers, representing the history of request state changes.
         * @param dataSourcingHistory - A list of DataSourcingWithoutReferences objects representing the history of
         * data sourcing state changes associated with the request.
         * @returns A list of CombinedHistoryEntryDefault objects representing the combined history of request states
         * and data sourcing states, with gaps filled and filtered for display.
         */
        fun buildCombinedHistory(
            dataRequestRevisions: List<Pair<RequestEntity, Long>>,
            dataSourcingHistory: List<DataSourcingWithoutReferences>,
        ): List<CombinedHistoryEntryDefault> {
            val requestEntries: List<CombinedHistoryEntryDefault> =
                dataRequestRevisions.map { (entity, _) ->
                    CombinedHistoryEntryDefault(
                        timestamp = entity.lastModifiedDate,
                        requestState = entity.state,
                        dataSourcingState = null,
                        displayedState = null,
                        adminComment = entity.adminComment,
                    )
                }

            val dataSourcingEntries: List<CombinedHistoryEntryDefault> =
                dataSourcingHistory.map { entry ->
                    CombinedHistoryEntryDefault(
                        timestamp = entry.lastModifiedDate,
                        requestState = null,
                        dataSourcingState = entry.state,
                        displayedState = null,
                        adminComment = null,
                    )
                }

            val combinedHistory = (requestEntries + dataSourcingEntries).sortedBy { it.timestamp }
            check(combinedHistory[0].requestState == RequestState.Open) {
                "The first entry in the history should have the request state 'Open'."
            }
            val filled = fillHistoryGaps(combinedHistory)
            return filterHistory(filled)
        }

        /**
         * Deletes consecutive entries with the same displayedState, keeping the order stable.
         *
         * @param entries - The combined history entries from which repeating displayed states should be removed.
         * @returns The filtered combined history entries with repeating displayed states removed.
         */
        fun deleteRepeatingDisplayedStates(entries: List<CombinedHistoryEntryDefault>): List<CombinedHistoryEntryDefault> {
            if (entries.isEmpty()) {
                return entries
            }

            val result = mutableListOf<CombinedHistoryEntryDefault>()
            var lastDisplayedState: DisplayedState? = null

            for (entry in entries) {
                val currentDisplayedState = entry.displayedState
                if (currentDisplayedState != lastDisplayedState) {
                    result.add(entry)
                    lastDisplayedState = currentDisplayedState
                }
            }

            return result
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
         * Retrieves the history of revisions for a specific data request identified by its ID.
         * @param requestId The UUID string of the data request whose history is to be retrieved.
         * @return A list of StoredRequest objects representing the revision history of the specified data request.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveBasicStateHistory(requestId: UUID): List<BasicStateHistoryEntry> {
            val dataRequestRevisions = dataRevisionRepository.listDataRequestRevisionsById(requestId)
            val dataSourcingID = getRequest(requestId).dataSourcingEntityId
            var dataSourcingHistory = emptyList<DataSourcingWithoutReferences>()
            if (dataSourcingID != null) {
                dataSourcingHistory =
                    dataSourcingManager.retrieveDataSourcingHistory(ValidationUtils.convertToUUID(dataSourcingID), true)
            }
            var combinedHistory = buildCombinedHistory(dataRequestRevisions, dataSourcingHistory)
            combinedHistory = deleteRepeatingDisplayedStates(combinedHistory)

            return combinedHistory.map { entry ->
                checkNotNull(entry.displayedState) {
                    "Displayed state should not be null after filling history gaps."
                }
                BasicStateHistoryEntryDefault(
                    modificationDate = entry.timestamp,
                    displayedState = entry.displayedState,
                )
            }
        }
    }
