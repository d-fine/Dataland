package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.openApiClient.api.RequestControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.AccessStatus
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
    private val mockKeycloakUserService = mock(KeycloakUserService::class.java)
    private val mockRequestControllerApi = mock(RequestControllerApi::class.java)

    private lateinit var requestPriorityUpdater: RequestPriorityUpdater

    private val premiumUserUUID = UUID.randomUUID()
    private val premiumUserId = premiumUserUUID.toString()
    private val normalUserUUID = UUID.randomUUID()
    private val normalUserId = normalUserUUID.toString()

    private val adminUser = KeycloakUserInfo("admin@dataland.com", premiumUserId, "Admin", "Doe")

    @BeforeEach
    fun setup() {
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
        `when`(mockKeycloakUserService.getUsersByRole("ROLE_PREMIUM_USER"))
            .thenReturn(listOf(adminUser))

        `when`(mockKeycloakUserService.getUsersByRole("ROLE_ADMIN"))
            .thenReturn(listOf())

        val requestIdPremiumUserPrioHigh = UUID.randomUUID()
        val requestIdPremiumUserPrioLow = UUID.randomUUID()
        val requestIdNormalUserPrioHigh = UUID.randomUUID()
        val requestIdNormalUserPrioLow = UUID.randomUUID()

        val extendedRequestPremiumUserPrioHigh = createRequest(premiumUserId, requestIdPremiumUserPrioHigh, RequestPriority.High)
        val extendedRequestPremiumUserPrioLow = createRequest(premiumUserId, requestIdPremiumUserPrioLow, RequestPriority.Low)
        val extendedRequestNormalUserPrioHigh = createRequest(normalUserId, requestIdNormalUserPrioHigh, RequestPriority.High)
        val extendedRequestNormalUserPrioLow = createRequest(normalUserId, requestIdNormalUserPrioLow, RequestPriority.Low)

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
            .patchDataRequest(dataRequestId = requestIdPremiumUserPrioLow, requestPriority = RequestPriority.High)

        verify(mockRequestControllerApi, times(1))
            .patchDataRequest(dataRequestId = requestIdNormalUserPrioHigh, requestPriority = RequestPriority.Low)

        verify(mockRequestControllerApi, never())
            .patchDataRequest(dataRequestId = requestIdPremiumUserPrioHigh, requestPriority = RequestPriority.Low)

        verify(mockRequestControllerApi, never())
            .patchDataRequest(dataRequestId = requestIdNormalUserPrioLow, requestPriority = RequestPriority.High)
    }
}
