package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.QaStatus
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.repositories.NotificationEventRepository
import org.dataland.datalandcommunitymanager.services.messaging.CompanyOwnershipClaimDatasetUploadedEmailBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatcher
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvestorRelationshipNotificationServiceTest {
    private lateinit var notificationEventRepository: NotificationEventRepository
    private lateinit var companyRolesManager: CompanyRolesManager
    private lateinit var companyDataControllerApi: CompanyDataControllerApi
    private lateinit var notificationEmailSender: CompanyOwnershipClaimDatasetUploadedEmailBuilder
    private lateinit var investorRelationshipNotificationService: InvestorRelationshipNotificationService

    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationService() {
        notificationEventRepository = mock(NotificationEventRepository::class.java)
        companyRolesManager = mock(CompanyRolesManager::class.java)
        companyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        notificationEmailSender = mock(CompanyOwnershipClaimDatasetUploadedEmailBuilder::class.java)

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

    @Test
    fun `Test processNotificationEvents`() {
        val companyMailList = listOf("mail@example.com")
        val notificationEventEntity =
            NotificationEventEntity(
                notificationEventType = NotificationEventType.InvestorRelationshipsEvent,
                userId = null,
                isProcessed = false,
                companyId = companyUUID,
                framework = DataTypeEnum.p2p,
                reportingPeriod = "2024",
            )
        `when`(companyRolesManager.getCompanyRoleAssignmentsByParameters(any(), any(), any()))
            .thenReturn(listOf(CompanyRoleAssignmentEntity(CompanyRole.CompanyOwner, companyUUID.toString(), "123")))
        `when`(companyDataControllerApi.getCompanyInfo(companyUUID.toString()))
            .thenReturn(CompanyInformation("Company", "", mapOf(), "DE", companyContactDetails = companyMailList))
        `when`(companyDataControllerApi.getCompanyInfo(any()))
            .thenReturn(CompanyInformation("", "", mapOf(), ""))

        val noNotificationEventEntity = notificationEventEntity.copy(companyId = UUID.randomUUID())
        val entityList = listOf(notificationEventEntity, notificationEventEntity, noNotificationEventEntity)
        val targetList = listOf(notificationEventEntity, notificationEventEntity)

        investorRelationshipNotificationService.processNotificationEvents(entityList)

        verify(notificationEmailSender).buildExternalAndInternalInvestorRelationshipSummaryEmailAndSendCEMessage(
            eq(targetList),
            eq(companyUUID),
            eq(companyMailList),
            any(),
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
