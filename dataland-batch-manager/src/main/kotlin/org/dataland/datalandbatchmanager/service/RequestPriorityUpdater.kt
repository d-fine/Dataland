package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.openApiClient.model.RequestPriority
import org.dataland.datalandcommunitymanager.openApiClient.model.RequestStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 */
@Service
class RequestPriorityUpdater(
    @Autowired private val keycloakUserService: KeycloakUserService,
    @Autowired private val requestControllerApi: RequestControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This function processes the request priority update
     */
    fun processRequestPriorityUpdates() {
        val premiumUserIds =
            keycloakUserService
                .getUsersByRole("ROLE_PREMIUM_USER")
                .map { it.userId }
                .toSet()
        val adminUserIDs =
            keycloakUserService
                .getUsersByRole("ROLE_ADMIN")
                .map { it.userId }
                .toSet()

        logger.info("Received PremiumUser IDs: $premiumUserIds")
        logger.info("Received Admin IDs: $adminUserIDs")

        updateRequestPriorities(
            currentPriority = RequestPriority.Low,
            newPriority = RequestPriority.High,
        ) { request -> request.userId in premiumUserIds }

        updateRequestPriorities(
            currentPriority = RequestPriority.High,
            newPriority = RequestPriority.Low,
        ) { request -> request.userId !in premiumUserIds }
    }

    /**
     * This function updates request priorities.
     */
    private fun updateRequestPriorities(
        currentPriority: RequestPriority,
        newPriority: RequestPriority,
        filterCondition: (ExtendedStoredDataRequest) -> Boolean,
    ) {
        val requests =
            requestControllerApi.getDataRequests(
                requestStatus = setOf(RequestStatus.Open),
                requestPriority = setOf(currentPriority),
            )
        requests
            .filter(filterCondition)
            .forEach { (dataRequestId) ->
                runCatching {
                    requestControllerApi.patchDataRequest(
                        dataRequestId = UUID.fromString(dataRequestId),
                        requestPriority = newPriority,
                    )
                }.onSuccess {
                    logger.info("Updated request priority of request $dataRequestId to $newPriority.")
                }.onFailure { e ->
                    logger.warn("Failed to update request priority of request $dataRequestId: ${e.message}")
                }
            }
    }
}
