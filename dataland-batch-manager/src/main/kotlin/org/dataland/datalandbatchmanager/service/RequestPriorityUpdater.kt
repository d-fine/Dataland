package org.dataland.datalandbatchmanager.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.RequestPriority
import org.dataland.dataSourcingService.openApiClient.model.RequestSearchFilterString
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.StoredRequest
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Service class for managing and updating the priorities of data requests in the Dataland community manager.
 */
@Service
class RequestPriorityUpdater
    @Autowired
    constructor(
        private val keycloakUserService: KeycloakUserService,
        private val requestControllerApi: RequestControllerApi,
        @Value("\${dataland.batch-manager.results-per-page:100}") private val resultsPerPage: Int,
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
            require(premiumUserIds.isNotEmpty()) {
                "No premium users or administrators found. Scheduled update of request priorities failed."
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
            filterCondition: (StoredRequest) -> Boolean,
        ) {
            val requests = getAllRequests(currentPriority)
            requests
                .filter(filterCondition)
                .forEach { (dataRequestId) ->
                    runCatching {
                        requestControllerApi.patchRequestPriority(
                            dataRequestId = dataRequestId,
                            requestPriority = newPriority,
                        )
                    }.onSuccess {
                        logger.info("Updated request priority of request $dataRequestId to $newPriority.")
                    }.onFailure { e ->
                        logger.warn("Failed to update request priority of request $dataRequestId: ${e.message}")
                    }
                }
        }

        /**
         * Gets all requests with the specified state and priority from the database and adds them to allRequests.
         */
        private fun getAllRequestsWithGivenStateAndPriority(
            requestState: RequestState,
            requestPriority: RequestPriority,
            expectedNumberOfRequests: Int,
            allRequests: MutableList<StoredRequest>,
        ) {
            var page = 0

            while (true) {
                val requestsWithSpecifiedState =
                    requestControllerApi.postRequestSearch(
                        requestSearchFilterString =
                            RequestSearchFilterString(
                                requestStates = listOf(requestState),
                                requestPriorities = listOf(requestPriority),
                            ),
                        chunkSize = resultsPerPage,
                        chunkIndex = page,
                    )
                allRequests.addAll(requestsWithSpecifiedState)

                if (requestsWithSpecifiedState.size < resultsPerPage || allRequests.size >= expectedNumberOfRequests) {
                    break
                }
                page++
            }
        }

        private fun getAllRequests(priority: RequestPriority): List<StoredRequest> {
            val expectedNumberOfOpenRequests =
                requestControllerApi.getNumberOfRequests(
                    requestState = RequestState.Open,
                    requestPriority = priority,
                )
            val expectedNumberOfProcessingRequests =
                requestControllerApi.getNumberOfRequests(
                    requestState = RequestState.Processing,
                    requestPriority = priority,
                )
            logger.info(
                "Found ${expectedNumberOfOpenRequests + expectedNumberOfProcessingRequests} requests with priority" +
                    "$priority to be considered for updating.",
            )
            val allRequests = mutableListOf<StoredRequest>()
            getAllRequestsWithGivenStateAndPriority(
                requestState = RequestState.Open,
                requestPriority = priority,
                expectedNumberOfRequests = expectedNumberOfOpenRequests,
                allRequests = allRequests,
            )
            getAllRequestsWithGivenStateAndPriority(
                requestState = RequestState.Processing,
                requestPriority = priority,
                expectedNumberOfRequests = expectedNumberOfProcessingRequests,
                allRequests = allRequests,
            )

            return allRequests
        }
    }
