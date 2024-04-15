package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.repositories.MessageRepository
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.e2etests.NUMBER_OF_SINGLE_DATA_REQUESTS_PER_DAY_AS_NON_PREMIUM_USER
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
class SingleDataRequestManagerQuotaTest(
    @Autowired val dataRequestRepository: DataRequestRepository,
    @Autowired val messageRepository: MessageRepository,
) {
    private lateinit var authenticationMock: DatalandJwtAuthentication
    private lateinit var singleDataRequestManager: SingleDataRequestManager

    private val companyIdRegexSafeCompanyId = UUID.randomUUID().toString()

    val allowedRequestsPerDay = NUMBER_OF_SINGLE_DATA_REQUESTS_PER_DAY_AS_NON_PREMIUM_USER
    val sampleRequest = SingleDataRequest(
        companyIdentifier = companyIdRegexSafeCompanyId,
        dataType = DataTypeEnum.lksg,
        reportingPeriods = setOf("1969"),
        contacts = setOf("testContact@example.com"),
        message = "Test message for non-premium user quota test",
    )

    private fun mockCompanyApiWithSingleCompany(): CompanyDataControllerApi {
        val companyApi = mock(CompanyDataControllerApi::class.java)
        `when`(companyApi.getCompaniesBySearchString(anyString(), anyInt())).thenReturn(
            listOf(
                CompanyIdAndName(
                    companyName = "Dummmy",
                    companyId = companyIdRegexSafeCompanyId,
                ),
            ),
        )
        return companyApi
    }

    private fun mockDataRequestProcessingUtils(): DataRequestProcessingUtils {
        val utils = DataRequestProcessingUtils(
            dataRequestRepository = dataRequestRepository,
            messageRepository = messageRepository,
            dataRequestLogger = mock(DataRequestLogger::class.java),
            companyApi = mockCompanyApiWithSingleCompany(),
        )
        return utils
    }

    @BeforeEach
    fun setupSingleDataRequestManager() {
        singleDataRequestManager = SingleDataRequestManager(
            dataRequestLogger = mock(DataRequestLogger::class.java),
            dataRequestRepository = dataRequestRepository,
            companyApi = mockCompanyApiWithSingleCompany(),
            singleDataRequestEmailMessageSender = mock(SingleDataRequestEmailMessageSender::class.java),
            utils = mockDataRequestProcessingUtils(),
        )
        val mockSecurityContext = mock(SecurityContext::class.java)
        `when`(mockSecurityContext.authentication).thenReturn(authenticationMock)
        `when`(authenticationMock.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `send single data requests as non-premium user and verify that the quota is met`() {
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@bigplayer.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_USER),
        )

        for (i in 1..allowedRequestsPerDay) {
            val passedRequest = sampleRequest.copy(reportingPeriods = setOf(i.toString()))
            assertDoesNotThrow { singleDataRequestManager.processSingleDataRequest(passedRequest) }
        }
        assertThrows<InsufficientRightsApiException> {
            singleDataRequestManager.processSingleDataRequest(sampleRequest)
        }
    }

    @Test
    fun `send single data requests as premium user and verify that the quota is not met`() {
        authenticationMock = AuthenticationMock.mockJwtAuthentication(
            "requester@bigplayer.com",
            "1234-221-1111elf",
            setOf(DatalandRealmRole.ROLE_PREMIUM_USER),
        )
        for (i in 1..allowedRequestsPerDay + 1) {
            val passedRequest = sampleRequest.copy(reportingPeriods = setOf(i.toString()))
            assertDoesNotThrow { singleDataRequestManager.processSingleDataRequest(passedRequest) }
        }
    }
}
