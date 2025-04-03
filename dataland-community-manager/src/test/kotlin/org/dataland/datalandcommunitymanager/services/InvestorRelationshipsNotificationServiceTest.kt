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
import org.dataland.datalandcommunitymanager.services.messaging.InvestorRelationshipsEmailBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.ArgumentMatcher
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InvestorRelationshipsNotificationServiceTest {
    private val mockNotificationEventRepository = mock<NotificationEventRepository>()
    private val mockCompanyRolesManager = mock<CompanyRolesManager>()
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockInvestorRelationshipEmailBuilder =
        mock<InvestorRelationshipsEmailBuilder>()
    private lateinit var investorRelationshipsNotificationService: InvestorRelationshipsNotificationService

    private val companyUUID = UUID.randomUUID()

    @BeforeEach
    fun setupNotificationService() {
        reset(
            mockNotificationEventRepository,
            mockCompanyRolesManager,
            mockCompanyDataControllerApi,
            mockInvestorRelationshipEmailBuilder,
        )

        investorRelationshipsNotificationService =
            InvestorRelationshipsNotificationService(
                mockNotificationEventRepository,
                mockCompanyRolesManager,
                mockCompanyDataControllerApi,
                mockInvestorRelationshipEmailBuilder,
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

        investorRelationshipsNotificationService.createCompanySpecificNotificationEvent(testDataMetaInformation)

        verifyNotificationEventRepositoryInteraction(notificationEventEntity)
    }

    private fun verifyNotificationEventRepositoryInteraction(notificationEventEntity: NotificationEventEntity) {
        verify(mockNotificationEventRepository, times(1)).save(
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
        doReturn(listOf(CompanyRoleAssignmentEntity(CompanyRole.CompanyOwner, companyUUID.toString(), "123")))
            .whenever(mockCompanyRolesManager)
            .getCompanyRoleAssignmentsByParameters(any(), any(), any())
        doReturn(CompanyInformation("Company", "", mapOf(), "DE", companyContactDetails = companyMailList))
            .whenever(mockCompanyDataControllerApi)
            .getCompanyInfo(companyUUID.toString())
        doReturn(CompanyInformation("", "", mapOf(), "")).whenever(mockCompanyDataControllerApi).getCompanyInfo(any())

        val noNotificationEventEntity = notificationEventEntity.copy(companyId = UUID.randomUUID())
        val entityList = listOf(notificationEventEntity, notificationEventEntity, noNotificationEventEntity)
        val targetList = listOf(notificationEventEntity, notificationEventEntity)

        investorRelationshipsNotificationService.processNotificationEvents(entityList)

        verify(mockInvestorRelationshipEmailBuilder)
            .buildExternalAndInternalInvestorRelationshipsSummaryEmailAndSendCEMessage(
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
