package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestClosedEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.GetDataRequestsSearchFilter
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.Duration
import java.time.Instant
import java.util.UUID

class DataRequestTimeSchedulerTest {
    private val testUtils = TestUtils()
    private lateinit var alterationManager: DataRequestAlterationManager
    private lateinit var dataRequestClosedEmailMessageSender: DataRequestClosedEmailMessageSender
    private lateinit var dataRequestRepository: DataRequestRepository
    private lateinit var dataRequestTimeScheduler: DataRequestTimeScheduler
    private val dataRequestIdStaleAndAnswered = UUID.randomUUID().toString()
    private val dummyDataRequestId = "dummyDataRequestId"
    private val staleDaysThreshold: Long = 10
    private val staleLastModified = Instant.now().minus(Duration.ofDays(staleDaysThreshold + 1)).toEpochMilli()
    private fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
    private fun getDataRequestEntity(
        requestId: String,
        status: RequestStatus,
        lastModifiedDate: Long,
    ): DataRequestEntity {
        return DataRequestEntity(
            dataRequestId = requestId,
            userId = "dummyUserId",
            creationTimestamp = 0,
            dataType = "dummyDataType",
            reportingPeriod = "dummyReportingPeriod",
            datalandCompanyId = "dummyCompanyId",
            messageHistory = emptyList(),
            lastModifiedDate = lastModifiedDate,
            requestStatus = status,
        )
    }

    @BeforeEach
    fun setUpDataRequestTimeScheduler() {
        testUtils.mockSecurityContext()
        alterationManager = mock(DataRequestAlterationManager::class.java)
        `when`(
            alterationManager
                .patchDataRequest(dataRequestIdStaleAndAnswered, RequestStatus.Closed),
        ).thenReturn(null)
        dataRequestClosedEmailMessageSender = mock(DataRequestClosedEmailMessageSender::class.java)
        doNothing().`when`(dataRequestClosedEmailMessageSender)
            .sendDataRequestClosedEmail(any(DataRequestEntity::class.java), anyString())
        dataRequestRepository = mock(DataRequestRepository::class.java)
        dataRequestTimeScheduler =
            DataRequestTimeScheduler(
                alterationManager,
                dataRequestClosedEmailMessageSender,
                dataRequestRepository,
                staleDaysThreshold,
            )
    }

    @Test
    fun `validate that two stale and answered data request is patched and an email is send`() {
        `when`(dataRequestRepository.searchDataRequestEntity(any(GetDataRequestsSearchFilter::class.java))).thenReturn(
            listOf(
                getDataRequestEntity(dataRequestIdStaleAndAnswered, RequestStatus.Answered, staleLastModified),
                getDataRequestEntity(dataRequestIdStaleAndAnswered, RequestStatus.Answered, staleLastModified),
            ),
        )
        dataRequestTimeScheduler.patchStaleAnsweredRequestToClosed()
        verify(dataRequestClosedEmailMessageSender, times(2))
            .sendDataRequestClosedEmail(
                any(DataRequestEntity::class.java), anyString(),
            )
        verify(alterationManager, times(2))
            .patchDataRequest(
                dataRequestIdStaleAndAnswered, RequestStatus.Closed,
            )
    }

    @Test
    fun `validate that recently modified data request are not patched and no email is send`() {
        reset(dataRequestRepository)
        val dataRequestEntities = mutableListOf<DataRequestEntity>()
        for (status in RequestStatus.entries) {
            val dataRequestEntity = getDataRequestEntity(dummyDataRequestId, status, Instant.now().toEpochMilli())
            dataRequestEntities.add(dataRequestEntity)
        }
        `when`(dataRequestRepository.searchDataRequestEntity(any(GetDataRequestsSearchFilter::class.java))).thenReturn(
            dataRequestEntities,
        )
        dataRequestTimeScheduler.patchStaleAnsweredRequestToClosed()

        verifyNoInteractions(dataRequestClosedEmailMessageSender)
        verifyNoInteractions(alterationManager)
    }
}
