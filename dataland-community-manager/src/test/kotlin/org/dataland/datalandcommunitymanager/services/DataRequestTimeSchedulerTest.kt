package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.kotlin.eq
import java.time.Duration
import java.time.Instant
import java.util.UUID

class DataRequestTimeSchedulerTest {
    private lateinit var mockDataRequestUpdateManager: DataRequestUpdateManager
    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var dataRequestTimeScheduler: DataRequestTimeScheduler
    private val dataRequestIdStaleAndAnswered = UUID.randomUUID().toString()
    private val dummyDataRequestId = "dummyDataRequestId"
    private val staleDaysThreshold: Long = 10
    private val staleLastModified = Instant.now().minus(Duration.ofDays(staleDaysThreshold + 1)).toEpochMilli()

    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)

    private fun getDataRequestEntity(
        requestId: String,
        status: RequestStatus,
        accessStatus: AccessStatus,
        lastModifiedDate: Long,
    ): DataRequestEntity {
        val dataRequestEntity =
            DataRequestEntity(
                dataRequestId = requestId,
                userId = "dummyUserId",
                creationTimestamp = 0,
                dataType = "dummyDataType",
                reportingPeriod = "dummyReportingPeriod",
                datalandCompanyId = "dummyCompanyId",
                emailOnUpdate = true,
                messageHistory = emptyList(),
                dataRequestStatusHistory = emptyList(),
                lastModifiedDate = lastModifiedDate,
                requestPriority = RequestPriority.Low,
                adminComment = "dummyAdminComment",
            )
        dataRequestEntity.dataRequestStatusHistory =
            listOf(
                RequestStatusEntity(
                    statusHistoryId = UUID.randomUUID().toString(),
                    requestStatus = status,
                    accessStatus = accessStatus,
                    creationTimestamp = 0,
                    dataRequest = dataRequestEntity,
                ),
            )
        return dataRequestEntity
    }

    @BeforeEach
    fun setUpDataRequestTimeScheduler() {
        TestUtils.mockSecurityContext()
        mockDataRequestUpdateManager = mock(DataRequestUpdateManager::class.java)
        `when`(
            mockDataRequestUpdateManager.patchDataRequest(
                dataRequestIdStaleAndAnswered,
                DataRequestPatch(requestStatus = RequestStatus.Closed),
                UUID.randomUUID().toString(),
            ),
        ).thenReturn(null)
        mockDataRequestRepository = mock(DataRequestRepository::class.java)
        dataRequestTimeScheduler =
            DataRequestTimeScheduler(
                mockDataRequestUpdateManager,
                mockDataRequestRepository,
                staleDaysThreshold,
            )
    }

    @Test
    fun `validate that two stale and answered data requests are patched`() {
        `when`(
            mockDataRequestRepository.searchDataRequestEntity(
                any(DataRequestsFilter::class.java),
                eq(100), eq(0),
            ),
        ).thenReturn(
            listOf(
                getDataRequestEntity(
                    dataRequestIdStaleAndAnswered, RequestStatus.Answered, AccessStatus.Public,
                    staleLastModified,
                ),
                getDataRequestEntity(
                    dataRequestIdStaleAndAnswered, RequestStatus.Answered, AccessStatus.Public,
                    staleLastModified,
                ),
            ),
        )
        dataRequestTimeScheduler.patchStaleAnsweredRequestToClosed()
        verify(mockDataRequestUpdateManager, times(2))
            .patchDataRequest(
                eq(dataRequestIdStaleAndAnswered),
                eq(DataRequestPatch(requestStatus = RequestStatus.Closed)),
                anyString(),
                eq(null),
            )
    }

    @Test
    fun `validate that recently modified data request are not patched`() {
        reset(mockDataRequestRepository)
        val dataRequestEntities = mutableListOf<DataRequestEntity>()
        for (status in RequestStatus.entries) {
            val dataRequestEntity =
                getDataRequestEntity(
                    dummyDataRequestId, status, AccessStatus.Public,
                    Instant.now().toEpochMilli(),
                )
            dataRequestEntities.add(dataRequestEntity)
        }
        `when`(
            mockDataRequestRepository.searchDataRequestEntity(
                any(DataRequestsFilter::class.java),
                eq(100), eq(0),
            ),
        ).thenReturn(
            dataRequestEntities,
        )
        dataRequestTimeScheduler.patchStaleAnsweredRequestToClosed()

        verifyNoInteractions(mockDataRequestUpdateManager)
    }
}
