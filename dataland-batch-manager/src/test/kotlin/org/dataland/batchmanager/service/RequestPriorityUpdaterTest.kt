package org.dataland.batchmanager.service

import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.RequestPriority
import org.dataland.dataSourcingService.openApiClient.model.RequestSearchFilterString
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.dataSourcingService.openApiClient.model.StoredRequest
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbatchmanager.service.RequestPriorityUpdater
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class RequestPriorityUpdaterTest {
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val mockRequestControllerApi = mock<RequestControllerApi>()
    private val resultsPerPage = 100
    private lateinit var requestPriorityUpdater: RequestPriorityUpdater

    companion object {
        private val premiumUserId = UUID.randomUUID().toString()
        private val normalUserId = UUID.randomUUID().toString()

        private val userIds = listOf(premiumUserId, normalUserId)
        private val requestPriorities = RequestPriority.entries.toList()
        private val requestStates = RequestState.entries.toList()

        private val numberOfTriples = userIds.size * requestPriorities.size * requestStates.size

        @JvmStatic
        @Suppress("UnusedPrivateMember") // detekt wrongly thinks this function is not used
        private fun provideIndexAndPriority(): List<Arguments> =
            (0..<numberOfTriples).flatMap { index ->
                RequestPriority.entries.map { priority -> Arguments.of(index, priority) }
            }
    }

    private fun userId(index: Int) = userIds[index % userIds.size]

    private fun requestPriority(index: Int) = requestPriorities[(index / userIds.size) % requestPriorities.size]

    private fun requestState(index: Int) = requestStates[(index / (userIds.size * requestPriorities.size)) % requestStates.size]

    private fun triple(index: Int): Triple<String, RequestPriority, RequestState> =
        Triple(userId(index), requestPriority(index), requestState(index))

    private val requestIdsMap =
        (0..<numberOfTriples).associate { index ->
            triple(index) to UUID.randomUUID()
        }

    private fun createRequest(
        userId: String,
        requestId: UUID,
        state: RequestState,
        priority: RequestPriority,
    ): StoredRequest =
        StoredRequest(
            id = requestId.toString(),
            companyId = UUID.randomUUID().toString(),
            userId = userId,
            reportingPeriod = "2025",
            dataType = "sfdr",
            creationTimeStamp = 0L,
            lastModifiedDate = 0L,
            requestPriority = priority,
            state = state,
        )

    private val storedRequestsMap =
        (0..<numberOfTriples).associate { index ->
            triple(index) to
                createRequest(
                    userId(index),
                    requestIdsMap[triple(index)] ?: UUID.randomUUID(),
                    requestState(index),
                    requestPriority(index),
                )
        }

    private val adminUser = KeycloakUserInfo("admin@dataland.com", premiumUserId, "Admin", "Doe")

    @BeforeEach
    fun setup() {
        reset(
            mockKeycloakUserService,
            mockRequestControllerApi,
        )

        requestPriorities.forEach { priority ->
            val matchingStoredRequests =
                storedRequestsMap.values.filter {
                    it.requestPriority == priority && it.state in setOf(RequestState.Open, RequestState.Processing)
                }
            doReturn(matchingStoredRequests)
                .whenever(mockRequestControllerApi)
                .postRequestSearch(
                    requestSearchFilterString =
                        RequestSearchFilterString(
                            requestStates = listOf(RequestState.Open, RequestState.Processing),
                            requestPriorities = listOf(priority),
                        ),
                    chunkSize = resultsPerPage,
                    chunkIndex = 0,
                )
            doReturn(matchingStoredRequests.size).whenever(mockRequestControllerApi).postRequestCountQuery(
                RequestSearchFilterString(
                    requestStates = listOf(RequestState.Open, RequestState.Processing),
                    requestPriorities = listOf(priority),
                ),
            )
        }

        requestPriorityUpdater =
            RequestPriorityUpdater(mockKeycloakUserService, mockRequestControllerApi, resultsPerPage)
    }

    @Test
    fun `check that the update priority process exits if the list of premium users is empty`() {
        doReturn(emptyList<KeycloakUserInfo>()).whenever(mockKeycloakUserService).getUsersByRole(any())
        assertThrows<IllegalArgumentException> { requestPriorityUpdater.processRequestPriorityUpdates() }
    }

    @ParameterizedTest
    @MethodSource("provideIndexAndPriority")
    fun `validate that the request priorities are updated correctly`(
        index: Int,
        priorityInPatch: RequestPriority,
    ) {
        doReturn(listOf(adminUser)).whenever(mockKeycloakUserService).getUsersByRole("ROLE_PREMIUM_USER")
        doReturn(emptyList<KeycloakUserInfo>()).whenever(mockKeycloakUserService).getUsersByRole("ROLE_ADMIN")

        requestPriorityUpdater.processRequestPriorityUpdates()

        val userId = userId(index)
        val requestPriority = requestPriority(index)
        val requestState = requestState(index)
        val requestId = requestIdsMap[triple(index)] ?: UUID.randomUUID()

        val requestPriorityToChange = if (userId == premiumUserId) RequestPriority.Low else RequestPriority.High
        val desiredPriorityAfterChange = if (userId == premiumUserId) RequestPriority.High else RequestPriority.Low

        val verificationMode =
            if (
                requestPriority == requestPriorityToChange &&
                priorityInPatch == desiredPriorityAfterChange &&
                requestState in setOf(RequestState.Open, RequestState.Processing)
            ) {
                times(1)
            } else {
                never()
            }

        verify(mockRequestControllerApi, verificationMode).patchRequestPriority(
            dataRequestId = requestId.toString(),
            requestPriority = priorityInPatch,
        )
    }
}
