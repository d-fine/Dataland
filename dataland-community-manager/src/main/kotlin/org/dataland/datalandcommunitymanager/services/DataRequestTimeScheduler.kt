package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestClosedEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID
/**
 * Implementation of a time scheduler for data requests
 * @param alterationManager DataRequestAlterationManager
 * @param dataRequestClosedEmailMessageSender DataRequestClosedEmailMessageSender
 * @param dataRequestRepository DataRequestRepository,
 * @param staleDaysThreshold limit for answered request to remain answered
 */

@Service("DataRequestTimeScheduler")
class DataRequestTimeScheduler(
    @Autowired private val alterationManager: DataRequestAlterationManager,
    @Autowired private val dataRequestClosedEmailMessageSender: DataRequestClosedEmailMessageSender,
    @Autowired private val dataRequestRepository: DataRequestRepository,
    @Value("\${dataland.community-manager.data-request.answered.stale-days-threshold}")
    private val staleDaysThreshold: Long,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Cron job that identifies stale answered requests, patches them to closed and triggers an email notification
     */

    @Suppress("UnusedPrivateMember") // Detect does not recognise the scheduled execution of this function
    @Scheduled(cron = "0 0 12 * * *")
    fun patchStaleAnsweredRequestToClosed() {
        val correlationId = UUID.randomUUID().toString()
        logger.info("Searching for stale answered data request. CorrelationId: $correlationId")
        val thresholdTime = Instant.now().minus(Duration.ofDays(staleDaysThreshold)).toEpochMilli()
        val searchFilterForAnsweredDataRequests = GetDataRequestsSearchFilter("", "", RequestStatus.Answered, "", "")
        val staleAnsweredRequests =
            dataRequestRepository.searchDataRequestEntity(searchFilterForAnsweredDataRequests)
                .filter { it.lastModifiedDate < thresholdTime }
        staleAnsweredRequests.forEach {
            println(it.requestStatus)
            logger.info(
                "Patching stale answered data request ${it.dataRequestId} to closed and " +
                    "informing user ${it.userId}. CorrelationId: $correlationId",
            )
            alterationManager.patchDataRequest(it.dataRequestId, RequestStatus.Closed)
            dataRequestClosedEmailMessageSender.sendDataRequestClosedEmail(
                it,
                correlationId,
            )
        }
    }
}
