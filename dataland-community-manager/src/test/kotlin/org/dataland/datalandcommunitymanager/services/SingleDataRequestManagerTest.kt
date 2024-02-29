package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
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
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
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
    private lateinit var dataRequestEmailMessageSenderMock: DataRequestEmailMessageSender
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var utilsMock: DataRequestProcessingUtils

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()

    @BeforeEach
    fun setupSingleDataRequestManager() {
        dataRequestEmailMessageSenderMock = mock(DataRequestEmailMessageSender::class.java)
        utilsMock = mockDataRequestProcessingUtils()
        val mockCompanyApi = mock(CompanyDataControllerApi::class.java)
        singleDataRequestManagerMock = SingleDataRequestManager(
            dataRequestLogger = mock(DataRequestLogger::class.java),
            companyApi = mockCompanyApi,
            dataRequestEmailMessageSender = dataRequestEmailMessageSenderMock,
            utils = utilsMock,
        )
        `when`(mockCompanyApi.getCompaniesBySearchString(anyString(), anyInt())).thenReturn(
            listOf(
                CompanyIdAndName(
                    companyName = "Dummmy",
                    companyId = companyIdRegexSafeCompanyId,
                ),
            ),
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
                requestStatus = RequestStatus.Open,
                dataType = (it.arguments[1] as DataTypeEnum).value,
                messageHistory = mutableListOf(),
                userId = "user-id",

            )
        }
        `when`(utilsMock.getDatalandCompanyIdForIdentifierValue(anyString()))
            .thenReturn(companyIdRegexSafeCompanyId)
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
        verify(dataRequestEmailMessageSenderMock, times(expectedExternalMessagesSent))
            .buildSingleDataRequestExternalMessage(
                anyString(),
                any() ?: authenticationMock,
                anyString(),
                any() ?: request.dataType,
                any() ?: request.reportingPeriods,
                any(),
            )
        verify(dataRequestEmailMessageSenderMock, times(expectedInternalMessagesSent))
            .buildSingleDataRequestInternalMessage(
                any() ?: authenticationMock,
                anyString(),
                any() ?: request.dataType,
                any() ?: request.reportingPeriods,
            )
    }
}
