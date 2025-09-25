package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.exceptions.DuplicateRequestException
import org.dataland.datasourcingservice.exceptions.RequestNotFoundApiException
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.utils.RequestLogger
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * Service responsible for managing data requests in the sense of the data sourcing service.
 */
@Service("SingleRequestManager")
class SingleRequestManager
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val requestRepository: RequestRepository,
        private val dataSourcingManager: DataSourcingManager,
        private val dataRevisionRepository: DataRevisionRepository,
    ) {
        private val requestLogger = RequestLogger()

        private fun getCompanyIdForIdentifier(identifier: String): UUID {
            val companyInformation =
                companyDataControllerApi.postCompanyValidation(listOf(identifier)).firstOrNull()?.companyInformation
                    ?: throw ResourceNotFoundApiException(
                        "The company identifier is unknown.",
                        "No company is associated to the identifier $identifier.",
                    )
            return UUID.fromString(companyInformation.companyId)
        }

        private fun storeRequest(
            userId: UUID,
            companyId: UUID,
            dataType: String,
            reportingPeriod: String,
            memberComment: String?,
        ): UUID {
            val dataRequestEntity =
                RequestEntity(
                    userId = userId,
                    companyId = companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    memberComment = memberComment,
                    creationTimestamp = Instant.now().toEpochMilli(),
                )

            return requestRepository
                .saveAndFlush(dataRequestEntity)
                .also {
                    requestLogger.logMessageForStoringDataRequest(it.id)
                }.id
        }

        private fun assertNoConflictingRequestExists(
            userId: UUID,
            companyId: UUID,
            dataType: String,
            reportingPeriod: String,
        ) {
            val duplicateRequests =
                requestRepository
                    .findByUserIdAndCompanyIdAndDataTypeAndReportingPeriod(
                        userId, companyId, dataType, reportingPeriod,
                    ).filter { it.state == RequestState.Open || it.state == RequestState.Processing }

            if (duplicateRequests.isNotEmpty()) {
                val duplicate = duplicateRequests.first()
                throw DuplicateRequestException(
                    duplicate.id,
                    duplicate.reportingPeriod,
                    duplicate.companyId,
                    duplicate.dataType,
                )
            }
        }

        /**
         * Creates a new data request based on the provided SingleRequest object.
         * In case a request for the same company, data type, reporting period, and user already exists
         * with a non-final state (i.e., Open or Processing), it will not create a new request for that reporting period.
         * @param singleRequest The SingleRequest object containing the details of the data request.
         * @param userId The UUID of the user making the request. If null, it will be extracted from the security context.
         * @return A SingleRequestResponse object containing details about the created request.
         * @throws ResourceNotFoundApiException If the specified company identifier does not exist.
         */
        @Transactional
        fun createRequest(
            singleRequest: SingleRequest,
            userId: UUID?,
        ): SingleRequestResponse {
            val userIdToUse = userId ?: UUID.fromString(DatalandAuthentication.fromContext().userId)

            val companyId = getCompanyIdForIdentifier(singleRequest.companyIdentifier)

            requestLogger.logMessageForReceivingSingleDataRequest(companyId, userIdToUse, UUID.randomUUID())
            assertNoConflictingRequestExists(userIdToUse, companyId, singleRequest.dataType, singleRequest.reportingPeriod)
            return SingleRequestResponse(
                storeRequest(
                    userId = userIdToUse,
                    companyId = companyId,
                    dataType = singleRequest.dataType,
                    reportingPeriod = singleRequest.reportingPeriod,
                    memberComment = singleRequest.memberComment,
                ).toString(),
            )
        }

        /**
         Retrieves a stored data request by its ID.
         * @param dataRequestId The UUID of the data request to retrieve.
         * @return The StoredRequest object corresponding to the given ID.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional(readOnly = true)
        fun getRequest(dataRequestId: UUID): StoredRequest =
            requestRepository.findById(dataRequestId).getOrNull()?.toStoredDataRequest()
                ?: throw RequestNotFoundApiException(
                    dataRequestId,
                )

        /**
         * Updates the state of a data request identified by its ID.
         * If the state is changed from Open to Processing, it ensures that a corresponding DataSourcingEntity exists.
         * @param dataRequestId The UUID of the data request to update.
         * @param newRequestState The new state to set for the data request.
         * @return The updated StoredRequest object.
         * @throws RequestNotFoundApiException If no data request with the given ID exists.
         */
        @Transactional
        fun patchRequestState(
            dataRequestId: UUID,
            newRequestState: RequestState,
        ): StoredRequest {
            val requestEntity =
                requestRepository.findById(dataRequestId).getOrNull() ?: throw RequestNotFoundApiException(
                    dataRequestId,
                )
            val oldRequestState = requestEntity.state
            requestEntity.state = newRequestState
            requestEntity.lastModifiedDate = Instant.now().toEpochMilli()

            if (oldRequestState == RequestState.Open && newRequestState == RequestState.Processing) {
                dataSourcingManager.resetOrCreateDataSourcingObjectAndAddRequest(requestEntity)
            } else {
                requestRepository.save(requestEntity)
            }
            return requestEntity.toStoredDataRequest()
        }

        /**
         * Retrieves the history of revisions for a specific data request identified by its ID.
         * @param id The UUID string of the data request whose history is to be retrieved.
         * @return A list of StoredRequest objects representing the revision history of the specified data request.
         * @throws InvalidInputApiException If the provided ID is not a valid UUID format.
         */
        @Transactional(readOnly = true)
        fun retrieveRequestHistory(id: String): List<StoredRequest> {
            val uuid =
                try {
                    UUID.fromString(id)
                } catch (_: IllegalArgumentException) {
                    throw InvalidInputApiException(
                        "Invalid UUID format for id: $id",
                        message = "Invalid UUID format for id: $id, please provide a valid UUID string.",
                    )
                }
            return dataRevisionRepository
                .listDataRequestRevisionsById(uuid)
                .map { it.toStoredDataRequest() }
        }
    }
