package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.CompanyIdValidator
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class SingleDataRequestManagerTest {
    private lateinit var singleDataRequestManager: SingleDataRequestManager
    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var mockSingleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender
    private lateinit var mockAuthentication: DatalandJwtAuthentication
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var mockSecurityUtilsService: SecurityUtilsService
    private lateinit var mockCompanyIdValidator: CompanyIdValidator
    private lateinit var mockAccessRequestEmailSender: AccessRequestEmailSender
    private lateinit var mockCompanyRolesManager: CompanyRolesManager
    private lateinit var mockDataAccessManager: DataAccessManager
    private lateinit var mockKeycloakUserService: KeycloakUserService

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()
    private val dummyCompanyIdAndName = CompanyIdAndName("Dummy Company AG", companyIdRegexSafeCompanyId)
    private val maxRequestsForUser = 10

    private val sampleRequest =
        SingleDataRequest(
            companyIdentifier = companyIdRegexSafeCompanyId,
            dataType = DataTypeEnum.lksg,
            reportingPeriods = setOf("1969"),
            contacts = setOf("testContact@example.com"),
            message = "Test message for non-premium user quota test",
        )
    private val testUtils = TestUtils()

    @BeforeEach
    fun setupSingleDataRequestManager() {
        mockSingleDataRequestEmailMessageSender = mock(SingleDataRequestEmailMessageSender::class.java)
        mockDataRequestProcessingUtils = createDataRequestProcessingUtilsMock()
        mockSecurityUtilsService = mock(SecurityUtilsService::class.java)
        mockCompanyIdValidator = mock(CompanyIdValidator::class.java)
        doNothing().`when`(mockCompanyIdValidator).checkIfCompanyIdIsValid(anyString())
        mockDataRequestRepository = createDataRequestRepositoryMock()
        mockAccessRequestEmailSender = mock(AccessRequestEmailSender::class.java)
        mockCompanyRolesManager = mock(CompanyRolesManager::class.java)
        mockDataAccessManager = mock(DataAccessManager::class.java)
        mockKeycloakUserService = mock(KeycloakUserService::class.java)
        singleDataRequestManager =
            SingleDataRequestManager(
                dataRequestLogger = mock(DataRequestLogger::class.java),
                dataRequestRepository = mockDataRequestRepository,
                companyIdValidator = mockCompanyIdValidator,
                singleDataRequestEmailMessageSender = mockSingleDataRequestEmailMessageSender,
                utils = mockDataRequestProcessingUtils,
                dataAccessManager = mockDataAccessManager,
                accessRequestEmailSender = mockAccessRequestEmailSender,
                securityUtilsService = mockSecurityUtilsService,
                companyRolesManager = mockCompanyRolesManager,
                keycloakUserService = mockKeycloakUserService,
                maxRequestsForUser = maxRequestsForUser,
            )

        val mockSecurityContext = createSecurityContextMock()
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun createSecurityContextMock(): SecurityContext {
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "requester@bigplayer.com",
                "1234-221-1111elf",
                setOf(DatalandRealmRole.ROLE_USER),
            )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        return mockSecurityContext
    }

    private fun createDataRequestRepositoryMock(): DataRequestRepository {
        var requestsCount = 0
        val dataRequestRepositoryMock = mock(DataRequestRepository::class.java)
        `when`(
            dataRequestRepositoryMock.getNumberOfDataRequestsPerformedByUserFromTimestamp(anyString(), anyLong()),
        ).then {
            requestsCount += 1
            return@then requestsCount - 1
        }
        return dataRequestRepositoryMock
    }

    private fun createDataRequestProcessingUtilsMock(): DataRequestProcessingUtils {
        val utilsMock = mock(DataRequestProcessingUtils::class.java)
        `when`(
            utilsMock.storeDataRequestEntityAsOpen(
                anyString(),
                anyString(),
                any(),
                anyString(),
                any(),
                any(),
            ),
        ).thenAnswer {
            DataRequestEntity(
                dataRequestId = "request-id",
                datalandCompanyId = it.arguments[0] as String,
                reportingPeriod = it.arguments[2] as String,
                creationTimestamp = 0,
                lastModifiedDate = 0,
                dataType = (it.arguments[1] as DataTypeEnum).value,
                messageHistory = mutableListOf(),
                dataRequestStatusHistory = emptyList(),
                userId = "user-id",
                requestPriority = RequestPriority.Low,
                adminComment = "dummyAdminComment",
            )
        }
        `when`(utilsMock.getDatalandCompanyIdAndNameForIdentifierValue(anyString(), anyBoolean())).thenReturn(
            dummyCompanyIdAndName,
        )
        return utilsMock
    }

    @Test
    fun `validate that an internal email message is sent if one contact is provided`() {
        testWhichEmailMessageIsSentFor(
            setOf("contact@othercompany.com"),
            "You forgot to upload data about the moon landing.",
            0,
            1,
        )
    }

    @Test
    fun `validate that two internal email messages are sent if one contact is provided`() {
        testWhichEmailMessageIsSentFor(
            setOf("contact@othercompany.com", "someoneelse@othercompany.com"),
            "You forgot to upload data about the moon landing.",
            0,
            2,
        )
    }

    @Test
    fun `validate that an internal email message is sent if no contact is provided as null`() {
        testWhichEmailMessageIsSentFor(null, null, 1, 0)
    }

    @Test
    fun `validate that an internal email message is sent if no contact is provided as empty set`() {
        testWhichEmailMessageIsSentFor(setOf(), null, 1, 0)
    }

    @Test
    fun `send single data requests as non premium user and verify that the quota is met`() {
        for (i in 1..maxRequestsForUser) {
            val passedRequest = sampleRequest.copy(reportingPeriods = setOf(i.toString()))
            assertDoesNotThrow { singleDataRequestManager.processSingleDataRequest(passedRequest) }
        }
        assertThrows<QuotaExceededException> {
            singleDataRequestManager.processSingleDataRequest(sampleRequest)
        }
    }

    @Test
    fun `send single data requests as premium user and verify that the quota is not applied`() {
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "requester@example.com",
                "1234-221-1111zwoelf",
                setOf(DatalandRealmRole.ROLE_PREMIUM_USER),
            )
        testUtils.mockSecurityContext()
        for (i in 1..maxRequestsForUser + 1) {
            val passedRequest = sampleRequest.copy(reportingPeriods = setOf(i.toString()))
            assertDoesNotThrow { singleDataRequestManager.processSingleDataRequest(passedRequest) }
        }
    }

    private fun testWhichEmailMessageIsSentFor(
        contacts: Set<String>?,
        message: String?,
        expectedInternalMessagesSent: Int,
        expectedExternalMessagesSent: Int,
    ) {
        val request =
            SingleDataRequest(
                companyIdentifier = companyIdRegexSafeCompanyId,
                dataType = DataTypeEnum.lksg,
                reportingPeriods = setOf("1969"),
                contacts = contacts,
                message = message,
            )
        singleDataRequestManager.processSingleDataRequest(
            request,
        )

        val numberOfTimesExternalMessageIsSend = if (expectedExternalMessagesSent >= 1) 1 else 0
        verify(
            mockSingleDataRequestEmailMessageSender,
            times(numberOfTimesExternalMessageIsSend),
        ).sendSingleDataRequestExternalMessage(
            any(),
            argThat { size == expectedExternalMessagesSent },
            any(),
            anyString(),
        )
        verify(
            mockSingleDataRequestEmailMessageSender,
            times(expectedInternalMessagesSent),
        ).sendSingleDataRequestInternalMessage(
            any(),
            anyString(),
        )
    }

    @Test
    fun `validate that access request is created and email is send`() {
        val reportingPeriod = "2020"
        val userId = "1234-221-1111elf"
        val contacts = setOf(MessageEntity.COMPANY_OWNER_KEYWORD)
        val message = "MESSAGE"
        val request =
            SingleDataRequest(
                companyIdentifier = companyIdRegexSafeCompanyId, dataType = DataTypeEnum.vsme,
                reportingPeriods = setOf(reportingPeriod), contacts = contacts, message = message,
            )

        `when`(
            mockCompanyRolesManager.getCompanyRoleAssignmentsByParameters(
                CompanyRole.CompanyOwner, companyIdRegexSafeCompanyId, null,
            ),
        ).thenReturn(
            listOf(CompanyRoleAssignmentEntity(CompanyRole.CompanyOwner, companyIdRegexSafeCompanyId, "123")),
        )

        `when`(
            mockDataRequestProcessingUtils.matchingDatasetExists(
                companyIdRegexSafeCompanyId,
                reportingPeriod,
                DataTypeEnum.vsme,
            ),
        ).thenReturn(true)

        `when`(
            mockDataAccessManager.hasAccessToPrivateDataset(
                companyIdRegexSafeCompanyId, reportingPeriod, DataTypeEnum.vsme, userId,
            ),
        ).thenReturn(false)

        singleDataRequestManager.processSingleDataRequest(request)

        verifyExpectedBehaviour(userId, reportingPeriod, contacts, message)
    }

    private fun verifyExpectedBehaviour(
        userId: String,
        reportingPeriod: String,
        contacts: Set<String>,
        message: String,
    ) {
        verify(mockDataAccessManager, times(1)).createAccessRequestToPrivateDataset(
            userId, companyIdRegexSafeCompanyId, DataTypeEnum.vsme, reportingPeriod, contacts, message,
        )

        verify(mockAccessRequestEmailSender, times(1)).notifyCompanyOwnerAboutNewRequest(any(), any())

        verifyNoInteractions(mockSingleDataRequestEmailMessageSender)

        verify(mockDataRequestProcessingUtils, times(0)).storeDataRequestEntityAsOpen(any(), any(), any(), any(), any())
    }

    @Test
    fun `check that requests are preprocessed with regard to the correct userIdToUse when no impersonation is used`() {
        val spySingleDataRequestManager = spy(singleDataRequestManager)
        val mockSingleDataRequest = mock(SingleDataRequest::class.java)
        val expectedUserIdToUse = DatalandAuthentication.fromContext().userId

        assertThrows<NullPointerException> {
            spySingleDataRequestManager.processSingleDataRequest(mockSingleDataRequest)
        }

        verify(spySingleDataRequestManager, times(1)).preprocessSingleDataRequest(any(), eq(expectedUserIdToUse))
    }

    @Test
    fun `check that requests are preprocessed with regard to the correct userIdToUse when impersonation is used`() {
        val spySingleDataRequestManager = spy(singleDataRequestManager)
        val mockSingleDataRequest = mock(SingleDataRequest::class.java)
        val expectedUserIdToUse = "impersonated-user-id"

        assertThrows<NullPointerException> {
            spySingleDataRequestManager.processSingleDataRequest(mockSingleDataRequest, expectedUserIdToUse)
        }

        verify(spySingleDataRequestManager, times(1)).preprocessSingleDataRequest(any(), eq(expectedUserIdToUse))
    }
}
