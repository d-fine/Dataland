package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequestMessageObject
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
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
    private lateinit var mockSingleDataRequestEmailSender: SingleDataRequestEmailSender
    private lateinit var mockAuthentication: DatalandJwtAuthentication

    private val companyIdRegexSafeCompanyId = "d623c5b6-ba18-23c3-1234-333555554444"

    @BeforeEach
    fun setupSingleDataRequestManager() {
        val mockObjectMapper = mockObjectMapper()
        val mockDataRequestRepository = mockDataRequestRepository()
        mockSingleDataRequestEmailSender = mock(SingleDataRequestEmailSender::class.java)
        singleDataRequestManager = SingleDataRequestManager(
            dataRequestRepository = mockDataRequestRepository,
            dataRequestLogger = mock(DataRequestLogger::class.java),
            companyGetter = mock(CompanyGetter::class.java),
            objectMapper = mockObjectMapper,
            singleDataRequestEmailSender = mockSingleDataRequestEmailSender,
        )
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication = AuthenticationMock.mockJwtAuthentication(
            "requester@bigplayer.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun mockDataRequestRepository(): DataRequestRepository {
        return mock(DataRequestRepository::class.java).also {
            `when`(
                it.existsByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeNameAndReportingPeriod(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                ),
            ).thenReturn(true)
        }
    }

    private fun mockObjectMapper(): ObjectMapper {
        return mock(ObjectMapper::class.java).also {
            `when`(
                it.readValue(
                    any() as String?,
                    any() ?: object : TypeReference<MutableList<StoredDataRequestMessageObject>>() {},
                ),
            ).thenReturn(
                mutableListOf(),
            )
        }
    }

    @Test
    fun `validate that an email is sent with a Dataland company ID provided`() {
        val request = SingleDataRequest(
            companyIdentifier = companyIdRegexSafeCompanyId,
            frameworkName = DataTypeEnum.lksg,
            listOfReportingPeriods = listOf("1969"),
            contactList = listOf("contact@othercompany.com"),
            message = "You forgot to upload data about the moon landing.",
        )
        singleDataRequestManager.processSingleDataRequest(
            request,
        )
        verify(mockSingleDataRequestEmailSender, times(1)).sendSingleDataRequestEmails(
            mockAuthentication,
            request,
            companyIdRegexSafeCompanyId,
        )
    }

    @Test
    fun `validate that an email is sent with an ISIN provided`() {
        val request = SingleDataRequest(
            companyIdentifier = "DK0083647253",
            frameworkName = DataTypeEnum.lksg,
            listOfReportingPeriods = listOf("1969"),
            contactList = listOf("contact@othercompany.com"),
            message = "You forgot to upload data about the moon landing.",
        )
        singleDataRequestManager.processSingleDataRequest(
            request,
        )
        verify(mockSingleDataRequestEmailSender, times(1)).sendSingleDataRequestEmails(
            mockAuthentication,
            request,
            request.companyIdentifier,
        )
    }
}
