package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

class SingleDataRequestManagerTest {

    private lateinit var singleDataRequestManager: SingleDataRequestManager
    private lateinit var singleDataRequestEmailSenderMock: SingleDataRequestEmailSender
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var utilsMock: DataRequestProcessingUtils

    private val companyIdRegexSafeCompanyId = "d623c5b6-ba18-23c3-1234-333555554444"

    @BeforeEach
    fun setupSingleDataRequestManager() {
        singleDataRequestEmailSenderMock = mock(SingleDataRequestEmailSender::class.java)
        utilsMock = mockDataRequestProcessingUtils()
        singleDataRequestManager = SingleDataRequestManager(
            dataRequestLogger = mock(DataRequestLogger::class.java),
            companyGetter = mock(CompanyGetter::class.java),
            singleDataRequestEmailSender = singleDataRequestEmailSenderMock,
            utils = utilsMock,
        )
        val mockSecurityContext = mock(SecurityContext::class.java)
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@bigplayer.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun mockDataRequestProcessingUtils(): DataRequestProcessingUtils {
        val utilsMock = mock(DataRequestProcessingUtils::class.java)
        `when`(
            utilsMock.storeDataRequestEntityIfNotExisting(
                anyString(),
                any() ?: DataRequestCompanyIdentifierType.DatalandCompanyId,
                any() ?: DataTypeEnum.lksg,
                anyString(),
                any(),
                any(),
            ),
        ).thenAnswer {
            DataRequestEntity(
                dataRequestId = "request-id",
                dataRequestCompanyIdentifierType = it.arguments[1] as DataRequestCompanyIdentifierType,
                dataRequestCompanyIdentifierValue = it.arguments[0] as String,
                reportingPeriod = it.arguments[3] as String,
                creationTimestamp = 0,
                lastModifiedDate = 0,
                requestStatus = RequestStatus.Open,
                dataType = (it.arguments[2] as DataTypeEnum).value,
                messageHistory = mutableListOf(),
                userId = "user-id",

            )
        }
        return utilsMock
    }

    @Test
    fun `validate that an email is sent with a Dataland company ID provided`() {
        val request = SingleDataRequest(
            companyIdentifier = companyIdRegexSafeCompanyId,
            frameworkName = DataTypeEnum.lksg,
            reportingPeriods = listOf("1969"),
            contacts = listOf("contact@othercompany.com"),
            message = "You forgot to upload data about the moon landing.",
        )
        `when`(utilsMock.determineIdentifierTypeViaRegex(anyString()))
            .thenReturn(DataRequestCompanyIdentifierType.DatalandCompanyId)
        `when`(utilsMock.getDatalandCompanyIdForIdentifierValue(anyString()))
            .thenReturn(companyIdRegexSafeCompanyId)
        singleDataRequestManager.processSingleDataRequest(
            request,
        )
        verify(singleDataRequestEmailSenderMock, times(1)).sendSingleDataRequestEmails(
            authenticationMock,
            request,
            DataRequestCompanyIdentifierType.DatalandCompanyId,
            companyIdRegexSafeCompanyId,
        )
    }

    @Test
    fun `validate that an email is sent with an ISIN provided`() {
        val isin = "DK0083647253"
        val request = SingleDataRequest(
            companyIdentifier = isin,
            frameworkName = DataTypeEnum.lksg,
            reportingPeriods = listOf("1969"),
            contacts = listOf("contact@othercompany.com"),
            message = "You forgot to upload data about the moon landing.",
        )
        `when`(utilsMock.determineIdentifierTypeViaRegex(anyString()))
            .thenReturn(DataRequestCompanyIdentifierType.Isin)
        `when`(utilsMock.getDatalandCompanyIdForIdentifierValue(anyString()))
            .thenReturn(null)
        singleDataRequestManager.processSingleDataRequest(
            request,
        )
        verify(singleDataRequestEmailSenderMock, times(1)).sendSingleDataRequestEmails(
            authenticationMock,
            request,
            DataRequestCompanyIdentifierType.Isin,
            request.companyIdentifier,
        )
    }
}
