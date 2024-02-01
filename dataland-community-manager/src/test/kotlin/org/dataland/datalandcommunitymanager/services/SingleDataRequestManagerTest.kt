package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class SingleDataRequestManagerTest(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val dataRequestLogger: DataRequestLogger,
) {

    private lateinit var singleDataRequestManager: SingleDataRequestManager
    private lateinit var mockCompanyGetter: CompanyGetter
    private lateinit var mockSingleDataRequestEmailSender: SingleDataRequestEmailSender
    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var mockAuthentication: DatalandJwtAuthentication

    private val companyIdRegexSafeCompanyId = "d623c5b6-ba18-23c3-1234-333555554444"

    @BeforeEach
    fun setupSingleDataRequestManager() {
        mockCompanyGetter = mock(CompanyGetter::class.java)
        mockSingleDataRequestEmailSender = mock(SingleDataRequestEmailSender::class.java)
        mockDataRequestRepository = mock(DataRequestRepository::class.java)
        singleDataRequestManager = SingleDataRequestManager(
            dataRequestRepository = mockDataRequestRepository,
            dataRequestLogger = dataRequestLogger,
            companyGetter = mockCompanyGetter,
            objectMapper = objectMapper,
            singleDataRequestEmailSender = mockSingleDataRequestEmailSender,
        )
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication = AuthenticationMock.mockJwtAuthentication("requester@bigplayer.com", "1234-221-1111elf", setOf(DatalandRealmRole.ROLE_USER))
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `validate that an email is sent with a Dataland company ID provided`() {
        prepareMockedDependenciesForEmailSentTest()
        val request = SingleDataRequest(
            companyIdentifier = companyIdRegexSafeCompanyId,
            frameworkName = DataTypeEnum.lksg,
            listOfReportingPeriods = listOf("1969"),
            contactList = listOf("contact@othercompany.com"),
            message = "You forgot to upload data about the moon landing."
        )
        singleDataRequestManager.processSingleDataRequest(
            request
        )
        verify(mockSingleDataRequestEmailSender, times(1)).sendSingleDataRequestEmails(
            mockAuthentication,
            request,
            DataRequestCompanyIdentifierType.DatalandCompanyId,
            companyIdRegexSafeCompanyId,
        )
    }

    @Test
    fun `validate that an email is sent with an ISIN provided`() {
        prepareMockedDependenciesForEmailSentTest()
        val request = SingleDataRequest(
            companyIdentifier = "DK0083647253",
            frameworkName = DataTypeEnum.lksg,
            listOfReportingPeriods = listOf("1969"),
            contactList = listOf("contact@othercompany.com"),
            message = "You forgot to upload data about the moon landing."
        )
        singleDataRequestManager.processSingleDataRequest(
            request
        )
        verify(mockSingleDataRequestEmailSender, times(1)).sendSingleDataRequestEmails(
            mockAuthentication,
            request,
            DataRequestCompanyIdentifierType.Isin,
            request.companyIdentifier,
        )
    }

    private fun prepareMockedDependenciesForEmailSentTest() {
        `when`(mockDataRequestRepository.existsByUserIdAndDataRequestCompanyIdentifierValueAndDataTypeNameAndReportingPeriod(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
        )).thenReturn(true)
    }
}