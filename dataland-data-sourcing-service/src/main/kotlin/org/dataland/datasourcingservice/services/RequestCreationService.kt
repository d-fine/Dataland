package org.dataland.datasourcingservice.services

import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.enums.RequestPriority
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.SingleRequest
import org.dataland.datasourcingservice.model.request.SingleRequestResponse
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.utils.DerivedRightsUtilsComponent
import org.dataland.datasourcingservice.utils.RequestLogger
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * Service class that manages all operations related to bulk requests.
 */
@Service("RequestCreationService")
class RequestCreationService
    @Autowired
    constructor(
        private val dataSourcingValidator: DataSourcingValidator,
        private val requestRepository: RequestRepository,
        private val derivedRightsUtilsComponent: DerivedRightsUtilsComponent,
        @Value("\${dataland.data-sourcing-service.max-number-of-data-requests-per-day-for-role-user}") val maxRequestsForUser: Int,
    ) {
        private val requestLogger = RequestLogger()

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
            val companyId = dataSourcingValidator.validateSingleDataRequest(singleRequest)

            requestLogger.logMessageForReceivingSingleDataRequest(companyId, userIdToUse, UUID.randomUUID())

            val idOfConflictingRequest =
                getIdOfConflictingRequest(
                    userIdToUse,
                    companyId,
                    singleRequest.dataType,
                    singleRequest.reportingPeriod,
                )
            return SingleRequestResponse(
                idOfConflictingRequest?.toString()
                    ?: storeRequest(
                        userId = userIdToUse,
                        BasicDataDimensions(
                            companyId = companyId.toString(),
                            dataType = singleRequest.dataType,
                            reportingPeriod = singleRequest.reportingPeriod,
                        ),
                        memberComment = singleRequest.memberComment,
                    ).toString(),
            )
        }

        /**
         * Stores a new data request in the database after performing a quota check for non-premium users.
         * @param userId the UUID of the user making the request
         * @param basicDataDimension the basic data dimensions associated with the request
         * @param memberComment an optional comment from the member
         * @return the UUID of the newly created data request
         * @throws QuotaExceededException if the user has exceeded their daily quota of requests
         */
        fun storeRequest(
            userId: UUID,
            basicDataDimension: BasicDataDimensions,
            memberComment: String? = null,
        ): UUID {
            val userIsPremiumUser = derivedRightsUtilsComponent.isUserPremiumUser(userId.toString())

            if (!userIsPremiumUser) performQuotaCheckForNonPremiumUser(userId)

            val dataRequestEntity =
                RequestEntity(
                    userId = userId,
                    companyId = UUID.fromString(basicDataDimension.companyId),
                    dataType = basicDataDimension.dataType,
                    reportingPeriod = basicDataDimension.reportingPeriod,
                    memberComment = memberComment,
                    creationTimestamp = Instant.now().toEpochMilli(),
                    requestPriority = if (userIsPremiumUser) RequestPriority.High else RequestPriority.Low,
                )

            return requestRepository
                .saveAndFlush(dataRequestEntity)
                .also {
                    requestLogger.logMessageForStoringDataRequest(it.id)
                }.id
        }

        private fun performQuotaCheckForNonPremiumUser(userId: UUID) {
            val numberOfDataRequestsPerformedByUserFromTimestamp =
                requestRepository.countByUserIdAndCreationTimestampGreaterThanEqual(
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

        private fun getEpochTimeStartOfDay(): Long =
            LocalDate
                .now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
    }
