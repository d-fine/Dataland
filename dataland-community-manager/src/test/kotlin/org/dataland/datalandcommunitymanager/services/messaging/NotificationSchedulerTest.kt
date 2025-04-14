package org.dataland.datalandcommunitymanager.services.messaging

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.DataRequestSummaryNotificationService
import org.dataland.datalandcommunitymanager.services.InvestorRelationsNotificationService
import org.dataland.datalandcommunitymanager.utils.NotificationUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationSchedulerTest {
    private val mockNotificationEventRepository = mock<NotificationEventRepository>()
    private lateinit var notificationUtils: NotificationUtils
    private val mockInvestorRelationsNotificationService = mock<InvestorRelationsNotificationService>()
    private val mockDataRequestSummaryNotificationService = mock<DataRequestSummaryNotificationService>()
    private lateinit var notificationScheduler: NotificationScheduler

    private val userUUID = UUID.randomUUID()
    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationScheduler() {
        reset(
            mockNotificationEventRepository,
            mockInvestorRelationsNotificationService,
            mockDataRequestSummaryNotificationService,
        )

        notificationUtils =
            NotificationUtils(
                mockNotificationEventRepository,
            )

        notificationScheduler =
            NotificationScheduler(
                mockNotificationEventRepository,
                notificationUtils,
                mockInvestorRelationsNotificationService,
                mockDataRequestSummaryNotificationService,
            )
    }

    @Test
    fun `test scheduledWeeklyEmailSending with no message`() {
        notificationScheduler.scheduledWeeklyEmailSending()

        verifyNoMoreInteractions(mockInvestorRelationsNotificationService)
        verifyNoMoreInteractions(mockDataRequestSummaryNotificationService)
    }

    @Test
    fun `test scheduledWeeklyEmailSending with messages`() {
        val notificationEventEntity =
            NotificationEventEntity(
                UUID.randomUUID(),
                NotificationEventType.AvailableEvent,
                userUUID,
                false,
                companyUUID,
                DataTypeEnum.lksg,
                "2024",
            )
        val entityList = listOf(notificationEventEntity, notificationEventEntity)
        doReturn(entityList)
            .whenever(mockNotificationEventRepository)
            .findAllByNotificationEventTypesAndIsProcessedFalse(any())

        notificationScheduler.scheduledWeeklyEmailSending()

        // One E-mail should be sent with both events for the same company
        verify(
            mockInvestorRelationsNotificationService, times(1),
        ).processNotificationEvents(entityList)
        verify(
            mockDataRequestSummaryNotificationService, times(1),
        ).processNotificationEvents(entityList)
        verify(mockNotificationEventRepository, times(2)).saveAll<NotificationEventEntity>(any())
    }
}
