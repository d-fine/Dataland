package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.exceptions.RequestNotFoundApiException
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.model.request.StoredRequest
import org.dataland.datasourcingservice.repositories.DataRevisionRepository
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.utils.RequestLogger
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.utils.KeycloakAdapterRequestProcessingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.collections.ifEmpty

/**
 * Service responsible for managing data requests in the sense of the data sourcing service.
 */
@Service("SingleRequestManager")
class SingleRequestManager
    @Autowired
    constructor(
        private val dataSourcingValidator: DataSourcingValidator,
        private val requestRepository: RequestRepository,
        private val dataSourcingManager: DataSourcingManager,
        private val dataRevisionRepository: DataRevisionRepository,
        private val keycloakAdapterRequestProcessingUtils: KeycloakAdapterRequestProcessingUtils,
        @Value("\${dataland.data-sourcing-service.max-number-of-data-requests-per-day-for-role-user}") val maxRequestsForUser: Int,
    ) {
        private val requestLogger = RequestLogger()

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

        private fun getIdOfConflictingRequest(
            userId: UUID,
            companyId: UUID,
            dataType: String,
            reportingPeriod: String,
        ): UUID? =
            requestRepository
                .findByUserIdAndCompanyIdAndDataTypeAndReportingPeriod(
                    userId, companyId, dataType, reportingPeriod,
                ).firstOrNull { it.state == RequestState.Open || it.state == RequestState.Processing }
                ?.id

        /**
         * Creates a new data request based on the provided SingleRequest object.
         * In case a request for the same company, data type, reporting period, and user already exists
         * with a non-final state (i.e., Open or Processing), it will not create a new request for that reporting period.
         * @param singleRequest The SingleRequest object containing the details of the data request.
         * @param userId The UUID of the user making the request. If null, it will be extracted from the security context.
         * @return A SingleRequestResponse object containing details about the created request.
         * @throws ResourceNotFoundApiException If the specified company identifier does not exist.
         * @throws QuotaExceededException If a non-premium user requests more requests than allowed.
         */
        @Transactional
        fun createRequest(
            singleRequest: SingleRequest,
            userId: UUID?,
        ): SingleRequestResponse {
            val userIdToUse = userId ?: UUID.fromString(DatalandAuthentication.fromContext().userId)

            val companyId =
                dataSourcingValidator
                    .validateAndGetCompanyId(singleRequest.companyIdentifier)
                    .getOrThrow()
            dataSourcingValidator.validateReportingPeriod(singleRequest.reportingPeriod).getOrThrow()

            requestLogger.logMessageForReceivingSingleDataRequest(companyId, userIdToUse, UUID.randomUUID())
            performQuotaCheckForNonPremiumUser(userId = userIdToUse)
            val idOfConflictingRequest =
                getIdOfConflictingRequest(userIdToUse, companyId, singleRequest.dataType.value, singleRequest.reportingPeriod)
            return if (idOfConflictingRequest != null) {
                SingleRequestResponse(
                    idOfConflictingRequest.toString(),
                )
            } else {
                SingleRequestResponse(
                    storeRequest(
                        userId = userIdToUse,
                        companyId = companyId,
                        dataType = singleRequest.dataType.value,
                        reportingPeriod = singleRequest.reportingPeriod,
                        memberComment = singleRequest.memberComment,
                    ).toString(),
                )
            }
        }

        private fun performQuotaCheckForNonPremiumUser(userId: UUID) {
            if (!keycloakAdapterRequestProcessingUtils.userIsPremiumUser(userId.toString())) {
                val numberOfDataRequestsPerformedByUserFromTimestamp =
                    requestRepository.getNumberOfRequestsOpenedByUserFromTimestamp(
                        userId, getEpochTimeStartOfDay(),
                    )
                if (numberOfDataRequestsPerformedByUserFromTimestamp + 1
                    > maxRequestsForUser
                ) {
                    throw QuotaExceededException(
                        "Quota has been reached.",
                        "The daily quota capacity has been reached.",
                    )
                }
            }
        }

        private fun getEpochTimeStartOfDay(): Long {
            val instantNow = Instant.ofEpochMilli(System.currentTimeMillis())
            val zoneId = ZoneId.systemDefault()
            val instantNowZoned = instantNow.atZone(zoneId)
            val startOfDay = instantNowZoned.toLocalDate().atStartOfDay(zoneId)
            return startOfDay.toInstant().toEpochMilli()
        }

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
        fun retrieveRequestHistory(requestId: String): List<StoredRequest> {
            val uuid =
                try {
                    UUID.fromString(requestId)
                } catch (_: IllegalArgumentException) {
                    throw InvalidInputApiException(
                        "Invalid UUID format for requestId: $requestId",
                        message = "Invalid UUID format for requestId: $requestId, please provide a valid UUID string.",
                    )
                }
            return dataRevisionRepository
                .listDataRequestRevisionsById(uuid)
                .map { it.toStoredDataRequest() }
                .ifEmpty {
                    throw RequestNotFoundApiException(uuid)
                }
        }
    }
