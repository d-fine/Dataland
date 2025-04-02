package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.entities.RequestStatusEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestStatusObject
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestSummaryEmailBuilder
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatcher
import org.mockito.Mockito.mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.util.UUID
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataRequestSummaryNotificationServiceTest {
    private val mockNotificationEventRepository = mock<NotificationEventRepository>()
    private val mockDataRequestSummaryEmailBuilder = mock<DataRequestSummaryEmailBuilder>()
    private lateinit var dataRequestSummaryNotificationService: DataRequestSummaryNotificationService

    private val userUUID = UUID.randomUUID()
    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationService() {
        reset(
            mockNotificationEventRepository,
            mockDataRequestSummaryEmailBuilder,
        )

        dataRequestSummaryNotificationService =
            DataRequestSummaryNotificationService(
                mockNotificationEventRepository, mockDataRequestSummaryEmailBuilder,
            )
    }

    @Test
    fun `Test scheduledWeeklyEmailSending with no message`() {
        dataRequestSummaryNotificationService.processNotificationEvents(listOf())

        verifyNoInteractions(mockDataRequestSummaryEmailBuilder)
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
        val noNotificationEventEntity = notificationEventEntity.copy(userId = null)
        val entityList = listOf(notificationEventEntity, notificationEventEntity, noNotificationEventEntity)
        val targetList = listOf(notificationEventEntity, notificationEventEntity)

        dataRequestSummaryNotificationService.processNotificationEvents(entityList)

        verify(
            mockDataRequestSummaryEmailBuilder, times(1),
        ).buildDataRequestSummaryEmailAndSendCEMessage(
            unprocessedEvents = eq(targetList),
            userId = eq(userUUID),
        )
    }

    @ParameterizedTest
    @MethodSource("provideInputForCreateUserSpecificNotificationEvent")
    fun `Test createUserSpecificNotificationEvent`(
        requestStatusBefore: RequestStatus,
        requestStatusAfter: RequestStatus?,
        immediateNotificationWasSent: Boolean,
        earlierQaApprovedVersionOfDatasetExists: Boolean,
        notificationEventType: NotificationEventType?,
    ) {
        TestUtils.mockSecurityContext()
        val dataRequestEntity =
            DataRequestEntity(
                userId = userUUID.toString(),
                creationTimestamp = 123,
                dataType = DataTypeEnum.lksg.toString(),
                reportingPeriod = "2024",
                datalandCompanyId = companyUUID.toString(),
                notifyMeImmediately = false,
            )
        val storedDataRequestStatusObject = StoredDataRequestStatusObject(requestStatusBefore, 123, AccessStatus.Public, null, null)
        val requestStatusEntity = RequestStatusEntity(storedDataRequestStatusObject, dataRequestEntity)
        dataRequestEntity.addToDataRequestStatusHistory(requestStatusEntity)

        dataRequestSummaryNotificationService.createUserSpecificNotificationEvent(
            dataRequestEntity,
            requestStatusAfter,
            immediateNotificationWasSent,
            earlierQaApprovedVersionOfDatasetExists,
        )

        if (notificationEventType == null) {
            verifyNoInteractions(mockNotificationEventRepository)
        } else {
            val notificationEventEntity =
                NotificationEventEntity(
                    notificationEventType = notificationEventType,
                    userId = userUUID,
                    isProcessed = immediateNotificationWasSent,
                    companyId = companyUUID,
                    framework = DataTypeEnum.lksg,
                    reportingPeriod = "2024",
                )
            verifyNotificationEventRepositoryInteraction(notificationEventEntity)
        }
    }

    private fun verifyNotificationEventRepositoryInteraction(notificationEventEntity: NotificationEventEntity) {
        verify(mockNotificationEventRepository, times(1)).save(
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
                    NotificationEventType.UpdatedEvent,
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
