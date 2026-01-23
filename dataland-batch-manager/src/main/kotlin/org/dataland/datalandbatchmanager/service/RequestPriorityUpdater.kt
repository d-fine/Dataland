package org.dataland.datalandbatchmanager.service

import org.dataland.dataSourcingService.openApiClient.api.MixedControllerApi
import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.MixedExtendedStoredRequest
import org.dataland.dataSourcingService.openApiClient.model.MixedRequestSearchFilterString
import org.dataland.dataSourcingService.openApiClient.model.RequestPriority
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
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
        private val companyRolesControllerApi: CompanyRolesControllerApi,
        private val keycloakUserService: KeycloakUserService,
        private val requestControllerApi: RequestControllerApi,
        private val mixedControllerApi: MixedControllerApi,
        private val derivedRightsUtilsComponent: DerivedRightsUtilsComponent,
        @Value("\${dataland.batch-manager.results-per-page:100}") private val resultsPerPage: Int,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Processes request priority updates for data requests.
         *
         * This method identifies Dataland members by their roles and updates the priority of their associated requests
         * to High. Similarly, it lowers the priority of requests for non-member users.
         */
        fun processRequestPriorityUpdates() {
            val userIdsOfAdmins = keycloakUserService.getUsersByRole("ROLE_ADMIN").map { it.userId }.toSet()
            val userIdsOfMembers =
                companyRolesControllerApi
                    .getExtendedCompanyRoleAssignments()
                    .map { it.userId }
                    .filter { derivedRightsUtilsComponent.isUserDatalandMember(it) }
                    .toSet()

            val userIdsOfAdminsAndMembers = userIdsOfAdmins + userIdsOfMembers
            require(userIdsOfAdminsAndMembers.isNotEmpty()) {
                "No Dataland admins or members found. Scheduled update of request priorities failed."
            }

            logger.info("Found ${userIdsOfAdminsAndMembers.size} users who are Dataland admins or members.")

            logger.info("Upgrading request priorities from Low to High for Dataland admins and members.")
            updateRequestPriorities(
                currentPriority = RequestPriority.Low,
                newPriority = RequestPriority.High,
            ) { request -> request.userId in userIdsOfAdminsAndMembers }

            logger.info("Downgrading request priorities from High to Low for users who are neither admins nor members.")
            updateRequestPriorities(
                currentPriority = RequestPriority.High,
                newPriority = RequestPriority.Low,
            ) { request -> request.userId !in userIdsOfAdminsAndMembers }
        }

        /**
         * Updates the priority of data requests based on specified criteria.
         *
         * @param currentPriority the current priority of requests to filter.
         * @param newPriority the new priority to assign to the filtered requests.
         * @param filterCondition a lambda function that determines whether a request's priority should be updated.
         *                        It takes a StoredRequest as input and returns a boolean.
         */
        private fun updateRequestPriorities(
            currentPriority: RequestPriority,
            newPriority: RequestPriority,
            filterCondition: (MixedExtendedStoredRequest) -> Boolean,
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

        private fun getAllRequests(priority: RequestPriority): List<MixedExtendedStoredRequest> {
            val expectedNumberOfRequests =
                mixedControllerApi.postRequestCountQuery(
                    MixedRequestSearchFilterString(
                        requestPriorities = listOf(priority),
                        requestStates = listOf(RequestState.Open, RequestState.Processing),
                    ),
                )
            logger.info(
                "Found $expectedNumberOfRequests requests with priority $priority to be considered for updating.",
            )
            val allRequests = mutableListOf<MixedExtendedStoredRequest>()

            var page = 0

            while (true) {
                val requestsWithSpecifiedState: Collection<MixedExtendedStoredRequest> =
                    mixedControllerApi.postRequestSearch(
                        mixedRequestSearchFilterString =
                            MixedRequestSearchFilterString(
                                requestStates = listOf(RequestState.Open, RequestState.Processing),
                                requestPriorities = listOf(priority),
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

            return allRequests
        }
    }
