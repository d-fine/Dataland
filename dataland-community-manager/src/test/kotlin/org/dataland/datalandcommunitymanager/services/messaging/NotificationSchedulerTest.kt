package org.dataland.datalandcommunitymanager.services.messaging

import NotificationScheduler
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.DataRequestSummaryNotificationService
import org.dataland.datalandcommunitymanager.services.InvestorRelationshipsNotificationService
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
    private val mockNotificationUtils = mock<NotificationUtils>()
    private val mockInvestorRelationshipsNotificationService = mock<InvestorRelationshipsNotificationService>()
    private val mockDataRequestSummaryNotificationService = mock<DataRequestSummaryNotificationService>()
    private lateinit var notificationScheduler: NotificationScheduler

    private val userUUID = UUID.randomUUID()
    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationScheduler() {
        reset(
            mockNotificationEventRepository,
            mockNotificationUtils,
            mockInvestorRelationshipsNotificationService,
            mockDataRequestSummaryNotificationService,
        )

        notificationScheduler =
            NotificationScheduler(
                mockNotificationEventRepository,
                mockNotificationUtils,
                mockInvestorRelationshipsNotificationService,
                mockDataRequestSummaryNotificationService,
            )
    }

    @Test
    fun `Test scheduledWeeklyEmailSending with no message`() {
        notificationScheduler.scheduledWeeklyEmailSending()

        verifyNoMoreInteractions(mockInvestorRelationshipsNotificationService)
        verifyNoMoreInteractions(mockDataRequestSummaryNotificationService)
    }

    @Test
    fun `Test scheduledWeeklyEmailSending with messages`() {
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
        /*
        val companyInformation =
            CompanyInformation(
                companyName = "Sample Company",
                headquarters = "headquarters",
                identifiers = mapOf(),
                countryCode = "DE",
                companyContactDetails = listOf("sampleCompany@example.com"),
            )
         */
        doReturn(entityList)
            .whenever(mockNotificationEventRepository)
            .findAllByNotificationEventTypesAndIsProcessedFalse(any())

        notificationScheduler.scheduledWeeklyEmailSending()

        // One E-mail should be sent with both events for the same company
        verify(
            mockInvestorRelationshipsNotificationService, times(1),
        ).processNotificationEvents(entityList)
        verify(
            mockDataRequestSummaryNotificationService, times(1),
        ).processNotificationEvents(entityList)
        verify(mockNotificationEventRepository, times(2)).saveAll<NotificationEventEntity>(any())
    }
}
