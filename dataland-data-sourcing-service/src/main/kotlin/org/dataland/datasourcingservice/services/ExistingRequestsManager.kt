package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datasourcingservice.exceptions.RequestNotFoundApiException
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
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
        private val dataSourcingManager: DataSourcingManager,
        private val dataRevisionRepository: DataRevisionRepository,
    ) {
        private val requestLogger = RequestLogger()

        /**
         Retrieves a stored data request by its ID.
         * @param dataRequestId The UUID of the data request to retrieve.
         * @return The StoredRequest object corresponding to the given ID.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional(readOnly = true)
        fun getRequest(dataRequestId: UUID): StoredRequest =
            requestRepository.findByIdAndFetchDataSourcingEntity(dataRequestId)?.toStoredDataRequest()
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
            requestEntity.lastModifiedDate = Instant.now().toEpochMilli()
            requestEntity.state = newRequestState

            if (adminComment != null) {
                requestLogger.logMessageForPatchingAdminComment(dataRequestId, adminComment)
                requestEntity.adminComment = adminComment
            }

            if (newRequestState == RequestState.Processing) {
                dataSourcingManager.resetOrCreateDataSourcingObjectAndAddRequest(requestEntity)
            } else {
                requestRepository.save(requestEntity)
            }
            return requestEntity.toStoredDataRequest()
        }

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
                .map { it.toStoredDataRequest() }
                .ifEmpty {
                    throw RequestNotFoundApiException(requestId)
                }
    }
