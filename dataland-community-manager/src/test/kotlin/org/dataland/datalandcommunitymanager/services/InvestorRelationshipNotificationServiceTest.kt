package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipClaimDatasetUploadedSender
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import org.mockito.ArgumentMatcher
import org.mockito.Mockito.mock
import org.mockito.kotlin.argThat
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.UUID
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvestorRelationshipNotificationServiceTest {
    private lateinit var notificationEventRepository: NotificationEventRepository
    private lateinit var companyRolesManager: CompanyRolesManager
    private lateinit var companyDataControllerApi: CompanyDataControllerApi
    private lateinit var notificationEmailSender: CompanyOwnershipClaimDatasetUploadedSender
    private lateinit var investorRelationshipNotificationService: InvestorRelationshipNotificationService

    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationService() {
        notificationEventRepository = mock(NotificationEventRepository::class.java)
        companyRolesManager = mock(CompanyRolesManager::class.java)
        companyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        notificationEmailSender = mock(CompanyOwnershipClaimDatasetUploadedSender::class.java)

        investorRelationshipNotificationService =
            InvestorRelationshipNotificationService(
                notificationEventRepository, companyRolesManager, companyDataControllerApi, notificationEmailSender,
            )
    }

    @Test
    fun `Test createCompanySpecificNotificationEvent`() {
        val testDataMetaInformation =
            DataMetaInformation(
                UUID.randomUUID().toString(),
                companyUUID.toString(),
                DataTypeEnum.p2p,
                123,
                "2024",
                false,
                QaStatus.Pending,
                "test",
            )
        val notificationEventEntity =
            NotificationEventEntity(
                notificationEventType = NotificationEventType.InvestorRelationshipsEvent,
                userId = null,
                isProcessed = false,
                companyId = companyUUID,
                framework = DataTypeEnum.p2p,
                reportingPeriod = "2024",
            )

        investorRelationshipNotificationService.createCompanySpecificNotificationEvent(testDataMetaInformation)

        verifyNotificationEventRepositoryInteraction(notificationEventEntity)
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
