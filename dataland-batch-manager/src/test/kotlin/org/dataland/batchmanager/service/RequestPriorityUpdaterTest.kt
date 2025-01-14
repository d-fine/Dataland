package org.dataland.batchmanager.service

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbatchmanager.service.RequestPriorityUpdater
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.AccessStatus
import org.dataland.datalandcommunitymanager.openApiClient.model.DataRequestPatch
import org.dataland.datalandcommunitymanager.openApiClient.model.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.openApiClient.model.RequestPriority
import org.dataland.datalandcommunitymanager.openApiClient.model.RequestStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class RequestPriorityUpdaterTest {
    private lateinit var mockKeycloakUserService: KeycloakUserService
    private lateinit var mockRequestControllerApi: RequestControllerApi
    private lateinit var requestPriorityUpdater: RequestPriorityUpdater

    private val premiumUserId = UUID.randomUUID().toString()
    private val normalUserId = UUID.randomUUID().toString()
    private val requestIdPremiumUserPrioHigh = UUID.randomUUID()
    private val requestIdPremiumUserPrioLow = UUID.randomUUID()
    private val requestIdNormalUserPrioHigh = UUID.randomUUID()
    private val requestIdNormalUserPrioLow = UUID.randomUUID()

    private val adminUser = KeycloakUserInfo("admin@dataland.com", premiumUserId, "Admin", "Doe")

    @BeforeEach
    fun setup() {
        mockKeycloakUserService = mock(KeycloakUserService::class.java)
        mockRequestControllerApi = mock(RequestControllerApi::class.java)
        requestPriorityUpdater = RequestPriorityUpdater(mockKeycloakUserService, mockRequestControllerApi)
    }

    private fun createRequest(
        userId: String,
        requestId: UUID,
        priority: RequestPriority,
    ): ExtendedStoredDataRequest =
        ExtendedStoredDataRequest(
            dataRequestId = requestId.toString(),
            userId = userId,
            reportingPeriod = "",
            creationTimestamp = 0,
            dataType = "sfdr",
            datalandCompanyId = "",
            companyName = "",
            lastModifiedDate = 0,
            requestStatus = RequestStatus.Open,
            accessStatus = AccessStatus.Public,
            requestPriority = priority,
        )

    @Test
    fun `validate the request priorities are updated correctly`() {
        val extendedRequestPremiumUserPrioHigh = createRequest(premiumUserId, requestIdPremiumUserPrioHigh, RequestPriority.High)
        val extendedRequestPremiumUserPrioLow = createRequest(premiumUserId, requestIdPremiumUserPrioLow, RequestPriority.Low)
        val extendedRequestNormalUserPrioHigh = createRequest(normalUserId, requestIdNormalUserPrioHigh, RequestPriority.High)
        val extendedRequestNormalUserPrioLow = createRequest(normalUserId, requestIdNormalUserPrioLow, RequestPriority.Low)
        val patchLow = DataRequestPatch(requestPriority = RequestPriority.Low)
        val patchHigh = DataRequestPatch(requestPriority = RequestPriority.High)

        `when`(mockKeycloakUserService.getUsersByRole("ROLE_PREMIUM_USER"))
            .thenReturn(listOf(adminUser))

        `when`(mockKeycloakUserService.getUsersByRole("ROLE_ADMIN"))
            .thenReturn(listOf())

        `when`(
            mockRequestControllerApi.getDataRequests(
                requestStatus = setOf(RequestStatus.Open),
                requestPriority = setOf(RequestPriority.Low),
            ),
        ).thenReturn(listOf(extendedRequestPremiumUserPrioLow, extendedRequestNormalUserPrioLow))

        `when`(
            mockRequestControllerApi.getDataRequests(
                requestStatus = setOf(RequestStatus.Open),
                requestPriority = setOf(RequestPriority.High),
            ),
        ).thenReturn(listOf(extendedRequestPremiumUserPrioHigh, extendedRequestNormalUserPrioHigh))

        requestPriorityUpdater.processRequestPriorityUpdates()

        verify(mockRequestControllerApi, times(1))
            .patchDataRequest(dataRequestId = requestIdPremiumUserPrioLow, dataRequestPatch = patchHigh)

        verify(mockRequestControllerApi, times(1))
            .patchDataRequest(dataRequestId = requestIdNormalUserPrioHigh, dataRequestPatch = patchLow)

        verify(mockRequestControllerApi, never())
            .patchDataRequest(dataRequestId = requestIdPremiumUserPrioHigh, dataRequestPatch = patchLow)

        verify(mockRequestControllerApi, never())
            .patchDataRequest(dataRequestId = requestIdNormalUserPrioLow, dataRequestPatch = patchHigh)
    }
}
