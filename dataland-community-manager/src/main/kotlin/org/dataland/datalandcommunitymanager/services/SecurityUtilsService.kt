package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Implements utility functions that can be used e.g., in PRE_AUTHORIZE
 * for several authentication use-cases
 */
@Service("SecurityUtilsService")
class SecurityUtilsService(
    @Autowired private val dataRequestRepository: DataRequestRepository,
) {
    /**
     * Returns true if and only if the currently authenticated user is asking for his/her own request
     */
    @Transactional
    fun isUserAskingForOwnRequest(requestId: UUID): Boolean {
        val userIdOfRequest = dataRequestRepository.findById(requestId.toString()).get().userId
        val userIdRequester = SecurityContextHolder.getContext().authentication.name
        return (userIdOfRequest == userIdRequester)
    }

    /**
     * Returns true if the request status is subject to change and conditions are met.
     * This is the case when the request is to be changed from answered to either closed or open
     * or if the status is changed to withdrawn.
     */
    @Transactional
    fun isRequestStatusChangeableByUser(
        requestId: UUID,
        requestStatusToPatch: RequestStatus,
    ): Boolean {
        val currentRequestStatus = dataRequestRepository.findById(requestId.toString()).get().requestStatus
        val statusChangeFromAnsweredToClosed =
            currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Closed
        val statusChangeFromAnsweredToOpen =
            currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Open
        val statusChangeFromAnsweredToWithdrawn =
            currentRequestStatus == RequestStatus.Answered && requestStatusToPatch == RequestStatus.Withdrawn
        val statusChangeFromOpenToWithdrawn =
            currentRequestStatus == RequestStatus.Open && requestStatusToPatch == RequestStatus.Withdrawn
        return (
            statusChangeFromAnsweredToClosed || statusChangeFromAnsweredToOpen ||
                statusChangeFromAnsweredToWithdrawn || statusChangeFromOpenToWithdrawn
            )
    }
}
