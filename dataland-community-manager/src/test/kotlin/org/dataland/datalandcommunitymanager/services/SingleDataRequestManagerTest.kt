package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.CompanyRoleAssignmentEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailBuilder
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageBuilder
import org.dataland.datalandcommunitymanager.utils.CommunityManagerDataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.KeycloakAdapterRequestProcessingUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class SingleDataRequestManagerTest {
    private val mockDataRequestRepository = mock<DataRequestRepository>()
    private val mockSingleDataRequestEmailMessageBuilder = mock<SingleDataRequestEmailMessageBuilder>()
    private val mockCommunityManagerDataRequestProcessingUtils = mock<CommunityManagerDataRequestProcessingUtils>()
    private val mockKeycloakAdapterRequestProcessingUtils = mock<KeycloakAdapterRequestProcessingUtils>()
    private val mockSecurityUtilsService = mock<SecurityUtilsService>()
    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val mockAccessRequestEmailBuilder = mock<AccessRequestEmailBuilder>()
    private val mockCompanyRolesManager = mock<CompanyRolesManager>()
    private val mockDataAccessManager = mock<DataAccessManager>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()

    private lateinit var singleDataRequestManager: SingleDataRequestManager

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()
    private val premiumUserId = UUID.randomUUID().toString()
    private val maxRequestsForUser = 10

    private val sampleRequest =
        SingleDataRequest(
            companyIdentifier = companyIdRegexSafeCompanyId,
            dataType = DataTypeEnum.lksg,
            notifyMeImmediately = false,
            reportingPeriods = setOf("1969"),
            contacts = setOf("testContact@example.com"),
            message = "Test message for non-premium user quota test",
        )

    @BeforeEach
    fun setupSingleDataRequestManager() {
        reset(
            mockDataRequestRepository,
            mockSingleDataRequestEmailMessageBuilder,
            mockCommunityManagerDataRequestProcessingUtils,
            mockSecurityUtilsService,
            mockCompanyInfoService,
            mockAccessRequestEmailBuilder,
            mockCompanyRolesManager,
            mockDataAccessManager,
            mockKeycloakUserService,
        )
        doNothing().whenever(mockCompanyInfoService).checkIfCompanyIdIsValid(anyString())
        doReturn(true).whenever(mockKeycloakAdapterRequestProcessingUtils).userIsPremiumUser(premiumUserId)
        setUpDataRequestRepositoryMock()
        doAnswer { invocation ->
            val identifiers = invocation.arguments[0] as List<String?>
            Pair(
                mapOf(
                    identifiers[0] to
                        CompanyIdAndName(
                            companyName = "",
                            companyId = identifiers[0] ?: UUID.randomUUID().toString(),
                        ),
                ),
                emptyList<String>(),
            )
        }.whenever(mockCommunityManagerDataRequestProcessingUtils).performIdentifierValidation(anyList())
        singleDataRequestManager =
            SingleDataRequestManager(
                dataRequestLogger = mock(DataRequestLogger::class.java),
                dataRequestRepository = mockDataRequestRepository,
                singleDataRequestEmailMessageBuilder = mockSingleDataRequestEmailMessageBuilder,
                communityManagerDataRequestProcessingUtils = mockCommunityManagerDataRequestProcessingUtils,
                keycloakAdapterRequestProcessingUtils = mockKeycloakAdapterRequestProcessingUtils,
                dataAccessManager = mockDataAccessManager,
                accessRequestEmailBuilder = mockAccessRequestEmailBuilder,
                securityUtilsService = mockSecurityUtilsService,
                companyRolesManager = mockCompanyRolesManager,
                maxRequestsForUser = maxRequestsForUser,
            )
        TestUtils.mockSecurityContext("requester@bigplayer.com", "1234-221-1111elf", DatalandRealmRole.ROLE_USER)
    }

    private fun setUpDataRequestRepositoryMock() {
        var requestsCount = 0
        whenever(
            mockDataRequestRepository.getNumberOfDataRequestsPerformedByUserFromTimestamp(anyString(), anyLong()),
        ).then {
            requestsCount += 1
            return@then requestsCount - 1
        }
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
        TestUtils.mockSecurityContext("data.premium-user@example.com", premiumUserId, DatalandRealmRole.ROLE_PREMIUM_USER)
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
            mockSingleDataRequestEmailMessageBuilder,
            times(numberOfTimesExternalMessageIsSend),
        ).buildSingleDataRequestExternalMessageAndSendCEMessage(
            any(),
            argThat { size == expectedExternalMessagesSent },
            any(),
            anyString(),
        )
        verify(
            mockSingleDataRequestEmailMessageBuilder,
            times(expectedInternalMessagesSent),
        ).buildSingleDataRequestInternalMessageAndSendCEMessage(
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
            mockCommunityManagerDataRequestProcessingUtils.matchingDatasetExists(
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

        verify(mockAccessRequestEmailBuilder, times(1)).notifyCompanyOwnerAboutNewRequest(any(), any())

        verifyNoInteractions(mockSingleDataRequestEmailMessageBuilder)

        verify(mockCommunityManagerDataRequestProcessingUtils, times(0)).storeDataRequestEntityAsOpen(
            userId = any(),
            datalandCompanyId = any(),
            dataType = any(),
            notifyMeImmediately = any(),
            reportingPeriod = any(),
            contacts = any(),
            message = any(),
        )
    }

    @Test
    fun `check that requests are preprocessed with regard to the correct userIdToUse when no impersonation is used`() {
        val spySingleDataRequestManager = spy(singleDataRequestManager)
        val mockSingleDataRequest = mock(SingleDataRequest::class.java)
        val expectedUserIdToUse = DatalandAuthentication.fromContext().userId

        assertThrows<InvalidInputApiException> {
            spySingleDataRequestManager.processSingleDataRequest(mockSingleDataRequest)
        }

        verify(spySingleDataRequestManager, times(1)).preprocessSingleDataRequest(any(), eq(expectedUserIdToUse))
    }

    @Test
    fun `check that requests are preprocessed with regard to the correct userIdToUse when impersonation is used`() {
        val spySingleDataRequestManager = spy(singleDataRequestManager)
        val mockSingleDataRequest = mock(SingleDataRequest::class.java)
        val expectedUserIdToUse = "impersonated-user-id"

        assertThrows<InvalidInputApiException> {
            spySingleDataRequestManager.processSingleDataRequest(mockSingleDataRequest, expectedUserIdToUse)
        }

        verify(spySingleDataRequestManager, times(1)).preprocessSingleDataRequest(any(), eq(expectedUserIdToUse))
    }
}
