package org.dataland.datalandcommunitymanager.services

import NotificationScheduler
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.utils.NotificationUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import org.mockito.ArgumentMatcher
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.UUID
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationSchedulerTest {
    private lateinit var notificationEventRepository: NotificationEventRepository
    private lateinit var notificationUtils: NotificationUtils
    private lateinit var investorRelationshipNotificationService: InvestorRelationshipNotificationService
    private lateinit var dataRequestSummaryNotificationService: DataRequestSummaryNotificationService
    private lateinit var notificationScheduler: NotificationScheduler

    private val userUUID = UUID.randomUUID()
    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationScheduler() {
        notificationEventRepository = mock(NotificationEventRepository::class.java)
        notificationUtils = mock(NotificationUtils::class.java)
        investorRelationshipNotificationService =
            mock(InvestorRelationshipNotificationService::class.java)
        dataRequestSummaryNotificationService = mock(DataRequestSummaryNotificationService::class.java)

        notificationScheduler =
            NotificationScheduler(
                notificationEventRepository,
                notificationUtils,
                investorRelationshipNotificationService,
                dataRequestSummaryNotificationService,
            )
    }

    @Test
    fun `Test scheduledWeeklyEmailSending with no message`() {
        notificationScheduler.scheduledWeeklyEmailSending()

        verifyNoMoreInteractions(notificationEmailSender)
        verifyNoMoreInteractions(dataRequestSummaryEmailSender)
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
        val companyInformation =
            CompanyInformation(
                companyName = "Sample Company",
                headquarters = "headquarters",
                identifiers = mapOf(),
                countryCode = "DE",
                companyContactDetails = listOf("sampleCompany@example.com"),
            )
        `when`(notificationEventRepository.findAllByNotificationEventTypesAndIsProcessedFalse(any()))
            .thenReturn(entityList)
        `when`(companyDataControllerApi.getCompanyInfo(companyUUID.toString())).thenReturn(companyInformation)

        notificationScheduler.scheduledWeeklyEmailSending()

        // One E-mail should be sent with both events for the same company
        verify(
            notificationEmailSender, times(1),
        ).sendExternalAndInternalInvestorRelationshipSummaryEmail(
            unprocessedEvents = eq(entityList),
            companyId = eq(companyUUID),
            receiver = eq(listOf("sampleCompany@example.com")),
            correlationId = any(),
        )
        verify(
            dataRequestSummaryEmailSender, times(1),
        ).sendDataRequestSummaryEmail(
            unprocessedEvents = eq(entityList),
            userId = eq(userUUID),
        )
        verify(notificationEventRepository, times(2)).saveAll<NotificationEventEntity>(any())
    }

    private fun verifyNotificationEventRepositoryInteraction(notificationEventEntity: NotificationEventEntity) {
        verify(notificationEventRepository, times(1)).save(
            argThat(NotificationEventEntityMatcher(notificationEventEntity)),
        )
    }

    companion object {
        @JvmStatic
        @Suppress("LongMethod")
        fun provideInputForCreateUserSpecificNotificationEvent(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    RequestStatus.Open,
                    RequestStatus.Answered,
                    true,
                    true,
                    NotificationEventType.UpdatedEvent,
                ),
                Arguments.of(
                    RequestStatus.Open,
                    RequestStatus.Answered,
                    false,
                    true,
                    NotificationEventType.UpdatedEvent,
                ),
                Arguments.of(
                    RequestStatus.Open,
                    RequestStatus.Answered,
                    true,
                    false,
                    NotificationEventType.AvailableEvent,
                ),
                Arguments.of(
                    RequestStatus.Open,
                    RequestStatus.Answered,
                    false,
                    false,
                    NotificationEventType.AvailableEvent,
                ),
                Arguments.of(
                    RequestStatus.Open,
                    RequestStatus.NonSourceable,
                    true,
                    true,
                    NotificationEventType.NonSourceableEvent,
                ),
                Arguments.of(
                    RequestStatus.Open,
                    RequestStatus.Withdrawn,
                    true,
                    true,
                    null,
                ),
                Arguments.of(
                    RequestStatus.NonSourceable,
                    RequestStatus.Answered,
                    true,
                    true,
                    NotificationEventType.UpdatedEvent,
                ),
                Arguments.of(
                    RequestStatus.Answered,
                    RequestStatus.Resolved,
                    true,
                    true,
                    null,
                ),
                Arguments.of(
                    RequestStatus.Answered,
                    RequestStatus.Closed,
                    true,
                    true,
                    null,
                ),
                Arguments.of(
                    RequestStatus.Answered,
                    RequestStatus.Open,
                    true,
                    true,
                    null,
                ),
                Arguments.of(
                    RequestStatus.Answered,
                    RequestStatus.Answered,
                    true,
                    true,
                    NotificationEventType.UpdatedEvent,
                ),
                Arguments.of(
                    RequestStatus.Resolved,
                    RequestStatus.Open,
                    true,
                    true,
                    null,
                ),
                Arguments.of(
                    RequestStatus.Resolved,
                    RequestStatus.Resolved,
                    true,
                    true,
                    NotificationEventType.UpdatedEvent,
                ),
                Arguments.of(
                    RequestStatus.Closed,
                    RequestStatus.Closed,
                    true,
                    true,
                    NotificationEventType.UpdatedEvent,
                ),
                Arguments.of(
                    RequestStatus.Closed,
                    null,
                    false,
                    false,
                    NotificationEventType.AvailableEvent,
                ),
            )
    }

    /**
     * This class compares two NotificationEventEntities.
     * Since the UUID and time are generated by default inside the class, they need to be excluded in the comparison.
     */
    class NotificationEventEntityMatcher(
        private val expected: NotificationEventEntity,
    ) : ArgumentMatcher<NotificationEventEntity> {
        override fun matches(given: NotificationEventEntity): Boolean =
            expected.notificationEventType == given.notificationEventType &&
                expected.userId == given.userId &&
                expected.isProcessed == given.isProcessed &&
                expected.companyId == given.companyId &&
                expected.framework == given.framework &&
                expected.reportingPeriod == given.reportingPeriod
    }
}
