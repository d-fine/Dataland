package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.QuotaExceededException
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.CompanyIdValidator
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

class SingleDataRequestManagerTest {

    private lateinit var singleDataRequestManagerMock: SingleDataRequestManager
    private lateinit var dataRequestRepositoryMock: DataRequestRepository
    private lateinit var singleDataRequestEmailMessageSenderMock: SingleDataRequestEmailMessageSender
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var utilsMock: DataRequestProcessingUtils
    private lateinit var mockCompanyIdValidator: CompanyIdValidator

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()
    private val dummyCompanyIdAndName = CompanyIdAndName("Dummy Company AG", companyIdRegexSafeCompanyId)
    private val maxRequestsForUser = 10

    private val sampleRequest = SingleDataRequest(
        companyIdentifier = companyIdRegexSafeCompanyId,
        dataType = DataTypeEnum.lksg,
        reportingPeriods = setOf("1969"),
        contacts = setOf("testContact@example.com"),
        message = "Test message for non-premium user quota test",
    )
    private val testUtils = TestUtils()

    @BeforeEach
    fun setupSingleDataRequestManager() {
        singleDataRequestEmailMessageSenderMock = mock(SingleDataRequestEmailMessageSender::class.java)
        utilsMock = createDataRequestProcessingUtilsMock()
        mockCompanyIdValidator = mock(CompanyIdValidator::class.java)
        dataRequestRepositoryMock = createDataRequestRepositoryMock()
        singleDataRequestManagerMock = SingleDataRequestManager(
            dataRequestLogger = mock(DataRequestLogger::class.java),
            dataRequestRepository = dataRequestRepositoryMock,
            companyIdValidator = mockCompanyIdValidator,
            singleDataRequestEmailMessageSender = singleDataRequestEmailMessageSenderMock,
            utils = utilsMock,
            maxRequestsForUser,
        )
        `when`(mockCompanyIdValidator.checkIfCompanyIdIsValidAndReturnName(anyString())).thenReturn("some-company-name")

        val mockSecurityContext = createSecurityContextMock()
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun createSecurityContextMock(): SecurityContext {
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@bigplayer.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        return mockSecurityContext
    }

    private fun createDataRequestRepositoryMock(): DataRequestRepository {
        var requestsCount = 0
        val dataRequestRepositoryMock = mock(DataRequestRepository::class.java)
        `when`(
            dataRequestRepositoryMock
                .getNumberOfDataRequestsPerformedByUserFromTimestamp(anyString(), anyLong()),
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
                any() ?: DataTypeEnum.lksg,
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
            )
        }
        `when`(utilsMock.getDatalandCompanyIdAndNameForIdentifierValue(anyString(), anyBoolean()))
            .thenReturn(dummyCompanyIdAndName)
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
            assertDoesNotThrow { singleDataRequestManagerMock.processSingleDataRequest(passedRequest) }
        }
        assertThrows<QuotaExceededException> {
            singleDataRequestManagerMock.processSingleDataRequest(sampleRequest)
        }
    }

    @Test
    fun `send single data requests as premium user and verify that the quota is not applied`() {
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@example.com",
            "1234-221-1111zwoelf",
            setOf(DatalandRealmRole.ROLE_PREMIUM_USER),
        )
        testUtils.mockSecurityContext()
        for (i in 1..maxRequestsForUser + 1) {
            val passedRequest = sampleRequest.copy(reportingPeriods = setOf(i.toString()))
            assertDoesNotThrow { singleDataRequestManagerMock.processSingleDataRequest(passedRequest) }
        }
    }

    private fun testWhichEmailMessageIsSentFor(
        contacts: Set<String>?,
        message: String?,
        expectedInternalMessagesSent: Int,
        expectedExternalMessagesSent: Int,
    ) {
        val request = SingleDataRequest(
            companyIdentifier = companyIdRegexSafeCompanyId,
            dataType = DataTypeEnum.lksg,
            reportingPeriods = setOf("1969"),
            contacts = contacts,
            message = message,
        )
        singleDataRequestManagerMock.processSingleDataRequest(
            request,
        )
        val dummyMessageInformation = SingleDataRequestEmailMessageSender.MessageInformation(
            dataType = DataTypeEnum.lksg, reportingPeriods = setOf("2024"),
            userAuthentication = authenticationMock, datalandCompanyId = companyIdRegexSafeCompanyId,
        )
        verify(singleDataRequestEmailMessageSenderMock, times(expectedExternalMessagesSent))
            .sendSingleDataRequestExternalMessage(
                any() ?: dummyMessageInformation,
                anyString(),
                any(),
                anyString(),
            )
        verify(singleDataRequestEmailMessageSenderMock, times(expectedInternalMessagesSent))
            .sendSingleDataRequestInternalMessage(
                any() ?: dummyMessageInformation,
                anyString(),
            )
    }
}
