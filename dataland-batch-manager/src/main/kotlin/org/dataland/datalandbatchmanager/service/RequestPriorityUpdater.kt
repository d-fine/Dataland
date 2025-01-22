package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.DataRequestPatch
import org.dataland.datalandcommunitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.openApiClient.model.RequestPriority
import org.dataland.datalandcommunitymanager.openApiClient.model.RequestStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

private const val RESULTS_PER_PAGE = 100

/**
 * Service class for managing and updating the priorities of data requests in the Dataland community manager.
 */
@Service
class RequestPriorityUpdater
    @Autowired
    constructor(
        private val keycloakUserService: KeycloakUserService,
        private val requestControllerApi: RequestControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Processes request priority updates for data requests.
         *
         * This method identifies premium users and administrators by their roles and updates the priority
         * of their associated requests to high. Similarly, it lowers the priority of requests for users
         * who do not belong to these roles.
         */
        fun processRequestPriorityUpdates() {
            val premiumUserIds = mutableSetOf<String>()
            for (roleName in listOf("ROLE_PREMIUM_USER", "ROLE_ADMIN")) {
                premiumUserIds.addAll(
                    keycloakUserService.getUsersByRole(roleName).map { it.userId },
                )
            }
            if (premiumUserIds.isEmpty()) {
                logger.error("No premium users or administrators found. Scheduled update of request priorities failed.")
                return
            }

            logger.info("Found ${premiumUserIds.size} premium users and administrators.")

            logger.info("Upgrading request priorities from Low to High for premium users.")
            updateRequestPriorities(
                currentPriority = RequestPriority.Low,
                newPriority = RequestPriority.High,
            ) { request -> request.userId in premiumUserIds }

            logger.info("Downgrading request priorities from High to Low for regular users.")
            updateRequestPriorities(
                currentPriority = RequestPriority.High,
                newPriority = RequestPriority.Low,
            ) { request -> request.userId !in premiumUserIds }
        }

        /**
         * Updates the priority of data requests based on specified criteria.
         *
         * @param currentPriority the current priority of requests to filter.
         * @param newPriority the new priority to assign to the filtered requests.
         * @param filterCondition a lambda function that determines whether a request's priority should be updated.
         *                        It takes an [ExtendedStoredDataRequest] as input and returns a boolean.
         */
        private fun updateRequestPriorities(
            currentPriority: RequestPriority,
            newPriority: RequestPriority,
            filterCondition: (ExtendedStoredDataRequest) -> Boolean,
        ) {
            val requests = getAllRequests(currentPriority)
            requests
                .filter(filterCondition)
                .forEach { (dataRequestId) ->
                    runCatching {
                        requestControllerApi.patchDataRequest(
                            dataRequestId = UUID.fromString(dataRequestId),
                            dataRequestPatch =
                                DataRequestPatch(
                                    requestPriority = newPriority,
                                ),
                        )
                    }.onSuccess {
                        logger.info("Updated request priority of request $dataRequestId to $newPriority.")
                    }.onFailure { e ->
                        logger.warn("Failed to update request priority of request $dataRequestId: ${e.message}")
                    }
                }
        }

        private fun getAllRequests(priority: RequestPriority): List<ExtendedStoredDataRequest> {
            val expectedRequests =
                requestControllerApi.getNumberOfRequests(
                    requestStatus = setOf(RequestStatus.Open),
                    requestPriority = setOf(priority),
                )
            logger.info("Found $expectedRequests requests with priority $priority to be considered for updating.")
            val allRequests = mutableListOf<ExtendedStoredDataRequest>()
            var page = 0

            while (true) {
                val requests =
                    requestControllerApi.getDataRequests(
                        requestStatus = setOf(RequestStatus.Open),
                        requestPriority = setOf(priority),
                        chunkSize = RESULTS_PER_PAGE,
                        chunkIndex = page,
                    )
                allRequests.addAll(requests)

                if (requests.size < RESULTS_PER_PAGE || allRequests.size >= expectedRequests) {
                    break
                }
                page++
            }

            return allRequests
        }
    }
