package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * Implementation of a time scheduler for data requests
 * @param dataRequestUpdateManager DataRequestAlterationManager
 * @param dataRequestRepository DataRequestRepository,
 */

@Service("DataRequestTimeScheduler")
class DataRequestTimeScheduler(
    @Autowired private val dataRequestUpdateManager: DataRequestUpdateManager,
    @Autowired private val dataRequestRepository: DataRequestRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Cron job that identifies stale answered requests, patches them to closed and triggers an email notification
     */
    @Scheduled(cron = "30 */1 * * * ?")
    fun patchStaleAnsweredRequestToClosed() {
        val correlationId = UUID.randomUUID().toString()
        logger.info("Searching for stale answered data request. CorrelationId: $correlationId")
        val searchFilterForAnsweredDataRequests =
            DataRequestsFilter(requestStatus = setOf(RequestStatus.Answered))
        val staleAnsweredRequests =
            dataRequestRepository
                .searchDataRequestEntity(searchFilterForAnsweredDataRequests)
                .filter { it.lastModifiedDate < Instant.now().toEpochMilli() }
        staleAnsweredRequests.forEach {
            logger.info(
                "Patching stale answered data request ${it.dataRequestId} to closed and " +
                    "informing user ${it.userId}. CorrelationId: $correlationId",
            )
            dataRequestUpdateManager.processExternalPatchRequestForDataRequest(
                it.dataRequestId,
                DataRequestPatch(requestStatus = RequestStatus.Closed),
                correlationId,
            )
        }
    }
}
