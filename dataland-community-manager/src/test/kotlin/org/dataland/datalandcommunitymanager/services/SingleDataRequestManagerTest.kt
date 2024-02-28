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
    private lateinit var singleDataRequestEmailSenderMock: SingleDataRequestEmailSender
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var utilsMock: DataRequestProcessingUtils

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()

    @BeforeEach
    fun setupSingleDataRequestManager() {
        singleDataRequestEmailSenderMock = mock(SingleDataRequestEmailSender::class.java)
        utilsMock = mockDataRequestProcessingUtils()
        val mockCompanyApi = mock(CompanyDataControllerApi::class.java)
        singleDataRequestManagerMock = SingleDataRequestManager(
            dataRequestLogger = mock(DataRequestLogger::class.java),
            companyApi = mockCompanyApi,
            singleDataRequestEmailSender = singleDataRequestEmailSenderMock,
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
                any() ?: "2023",
                any(),
                anyString(),
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
        return utilsMock
    }

    @Test
    fun `validate that an email is sent for a Dataland company ID provided`() {
        val request = SingleDataRequest(
            companyIdentifier = companyIdRegexSafeCompanyId,
            dataType = DataTypeEnum.lksg.value,
            reportingPeriods = setOf("1969"),
            contacts = setOf("contact@othercompany.com"),
            message = "You forgot to upload data about the moon landing.",
        )
        `when`(utilsMock.getDatalandCompanyIdForIdentifierValue(anyString()))
            .thenReturn(companyIdRegexSafeCompanyId)
        singleDataRequestManagerMock.processSingleDataRequest(
            request,
        )
        verify(singleDataRequestEmailSenderMock, times(1)).sendSingleDataRequestEmails(
            authenticationMock,
            request,
            companyIdRegexSafeCompanyId,
        )
    }
}
