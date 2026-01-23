package org.dataland.batchmanager.service

import org.dataland.dataSourcingService.openApiClient.api.MixedControllerApi
import org.dataland.dataSourcingService.openApiClient.api.RequestControllerApi
import org.dataland.dataSourcingService.openApiClient.model.MixedExtendedStoredRequest
import org.dataland.dataSourcingService.openApiClient.model.MixedRequestSearchFilterString
import org.dataland.dataSourcingService.openApiClient.model.RequestPriority
import org.dataland.dataSourcingService.openApiClient.model.RequestState
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbatchmanager.service.DerivedRightsUtilsComponent
import org.dataland.datalandbatchmanager.service.RequestPriorityUpdater
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRoleAssignmentExtended
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class RequestPriorityUpdaterTest {
    private val mockCompanyRolesControllerApi = mock<CompanyRolesControllerApi>()
    private val mockRequestControllerApi = mock<RequestControllerApi>()
    private val mockMixedControllerApi = mock<MixedControllerApi>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val mockDerivedRightsUtilsComponent = mock<DerivedRightsUtilsComponent>()

    private val resultsPerPage = 100
    private lateinit var requestPriorityUpdater: RequestPriorityUpdater

    companion object {
        private val memberUserId = UUID.randomUUID().toString()
        private val nonMemberUserId = UUID.randomUUID().toString()

        private val userIds = listOf(memberUserId, nonMemberUserId)
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
    ): MixedExtendedStoredRequest =
        MixedExtendedStoredRequest(
            id = requestId.toString(),
            companyId = UUID.randomUUID().toString(),
            userId = userId,
            reportingPeriod = "2025",
            dataType = "sfdr",
            creationTimestamp = 0L,
            lastModifiedDate = 0L,
            requestPriority = priority,
            state = state,
            companyName = "Dummy Company",
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

    @BeforeEach
    fun setup() {
        reset(
            mockCompanyRolesControllerApi,
            mockRequestControllerApi,
            mockMixedControllerApi,
            mockKeycloakUserService,
            mockDerivedRightsUtilsComponent,
        )

        requestPriorities.forEach { priority ->
            val matchingStoredRequests =
                storedRequestsMap.values.filter {
                    it.requestPriority == priority && it.state in setOf(RequestState.Open, RequestState.Processing)
                }
            doReturn(matchingStoredRequests)
                .whenever(mockMixedControllerApi)
                .postRequestSearch(
                    mixedRequestSearchFilterString =
                        MixedRequestSearchFilterString(
                            requestStates = listOf(RequestState.Open, RequestState.Processing),
                            requestPriorities = listOf(priority),
                        ),
                    chunkSize = resultsPerPage,
                    chunkIndex = 0,
                )
            doReturn(matchingStoredRequests.size).whenever(mockMixedControllerApi).postRequestCountQuery(
                MixedRequestSearchFilterString(
                    requestStates = listOf(RequestState.Open, RequestState.Processing),
                    requestPriorities = listOf(priority),
                ),
            )
        }

        requestPriorityUpdater =
            RequestPriorityUpdater(
                companyRolesControllerApi = mockCompanyRolesControllerApi,
                keycloakUserService = mockKeycloakUserService,
                requestControllerApi = mockRequestControllerApi,
                mixedControllerApi = mockMixedControllerApi,
                derivedRightsUtilsComponent = mockDerivedRightsUtilsComponent,
                resultsPerPage = resultsPerPage,
            )
    }

    @Test
    fun `check that the update priority process exits if there are no admins nor members`() {
        doReturn(emptyList<CompanyRoleAssignmentExtended>())
            .whenever(mockCompanyRolesControllerApi)
            .getExtendedCompanyRoleAssignments(anyOrNull(), anyOrNull(), anyOrNull())
        assertThrows<IllegalArgumentException> { requestPriorityUpdater.processRequestPriorityUpdates() }
    }

    @ParameterizedTest
    @MethodSource("provideIndexAndPriority")
    fun `validate that the request priorities are updated correctly`(
        index: Int,
        priorityInPatch: RequestPriority,
    ) {
        doReturn(
            listOf(
                CompanyRoleAssignmentExtended(
                    companyRole = CompanyRole.Analyst,
                    companyId = UUID.randomUUID().toString(),
                    userId = memberUserId.toString(),
                    email = "test@example.com",
                    firstName = "Jane",
                    lastName = "Doe",
                ),
            ),
        ).whenever(mockCompanyRolesControllerApi).getExtendedCompanyRoleAssignments(
            role = null,
            companyId = null,
            userId = null,
        )

        doReturn(true).whenever(mockDerivedRightsUtilsComponent).isUserDatalandMember(memberUserId)

        requestPriorityUpdater.processRequestPriorityUpdates()

        val userId = userId(index)
        val requestPriority = requestPriority(index)
        val requestState = requestState(index)
        val requestId = requestIdsMap[triple(index)] ?: UUID.randomUUID()

        val requestPriorityToChange = if (userId == memberUserId) RequestPriority.Low else RequestPriority.High
        val desiredPriorityAfterChange = if (userId == memberUserId) RequestPriority.High else RequestPriority.Low

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
