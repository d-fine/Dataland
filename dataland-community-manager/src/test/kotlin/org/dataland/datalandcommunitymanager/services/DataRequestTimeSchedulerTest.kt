package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant
import java.util.UUID

class DataRequestTimeSchedulerTest {
    private val mockDataRequestUpdateManager = mock<DataRequestUpdateManager>()
    private val mockDataRequestRepository = mock<DataRequestRepository>()
    private lateinit var dataRequestTimeScheduler: DataRequestTimeScheduler
    private val dataRequestIdStaleAndAnswered = UUID.randomUUID().toString()
    private val dummyDataRequestId = "dummyDataRequestId"
    private val staleDaysThreshold: Long = 10
    private val staleLastModified = Instant.now().minus(Duration.ofDays(staleDaysThreshold + 1)).toEpochMilli()

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
                notifyMeImmediately = true,
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
        reset(mockDataRequestRepository)
        TestUtils.mockSecurityContext()
        doReturn(null)
            .whenever(
                mockDataRequestUpdateManager,
            ).processExternalPatchRequestForDataRequest(
                dataRequestIdStaleAndAnswered,
                DataRequestPatch(requestStatus = RequestStatus.Closed),
                UUID.randomUUID().toString(),
            )
        dataRequestTimeScheduler =
            DataRequestTimeScheduler(
                mockDataRequestUpdateManager,
                mockDataRequestRepository,
                staleDaysThreshold,
            )
    }

    @Test
    fun `validate that two stale and answered data requests are patched`() {
        doReturn(
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
        ).whenever(mockDataRequestRepository)
            .searchDataRequestEntity(
                any(), anyOrNull(),
                eq(100), eq(0),
            )
        dataRequestTimeScheduler.patchStaleAnsweredRequestToClosed()
        verify(mockDataRequestUpdateManager, times(2))
            .processExternalPatchRequestForDataRequest(
                eq(dataRequestIdStaleAndAnswered),
                eq(DataRequestPatch(requestStatus = RequestStatus.Closed)),
                anyString(),
            )
    }

    @Test
    fun `validate that recently modified data request are not patched`() {
        val dataRequestEntities = mutableListOf<DataRequestEntity>()
        for (status in RequestStatus.entries) {
            val dataRequestEntity =
                getDataRequestEntity(
                    dummyDataRequestId, status, AccessStatus.Public,
                    Instant.now().toEpochMilli(),
                )
            dataRequestEntities.add(dataRequestEntity)
        }
        doReturn(dataRequestEntities)
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(
                any(), any(),
                eq(100), eq(0),
            )
        dataRequestTimeScheduler.patchStaleAnsweredRequestToClosed()

        verifyNoInteractions(mockDataRequestUpdateManager)
    }
}
