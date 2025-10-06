package org.dataland.datasourcingservice.services

import org.dataland.datalandbackend.openApiClient.model.BasicDataDimensions
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.repositories.RequestRepository
import org.dataland.datasourcingservice.utils.RequestLogger
import org.dataland.keycloakAdapter.utils.KeycloakAdapterRequestProcessingUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

/**
 * Service class that manages all operations related to bulk requests.
 */
@Service("RequestCreationService")
class RequestCreationService
    @Autowired
    constructor(
        private val requestRepository: RequestRepository,
        private val keycloakAdapterRequestProcessingUtils: KeycloakAdapterRequestProcessingUtils,
        @Value("\${dataland.data-sourcing-service.max-number-of-data-requests-per-day-for-role-user}") val maxRequestsForUser: Int,
    ) {
        private val requestLogger = RequestLogger()

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
            performQuotaCheckForNonPremiumUser(userId)
            val dataRequestEntity =
                RequestEntity(
                    userId = userId,
                    companyId = UUID.fromString(basicDataDimension.companyId),
                    dataType = basicDataDimension.dataType,
                    reportingPeriod = basicDataDimension.reportingPeriod,
                    memberComment = memberComment,
                    creationTimestamp = Instant.now().toEpochMilli(),
                )

            return requestRepository
                .saveAndFlush(dataRequestEntity)
                .also {
                    requestLogger.logMessageForStoringDataRequest(it.id)
                }.id
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
    }
