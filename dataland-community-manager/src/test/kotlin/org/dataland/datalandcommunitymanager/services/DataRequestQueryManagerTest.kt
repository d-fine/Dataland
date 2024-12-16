package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestMasker
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Instant
import java.util.UUID

/**
 * Tests the getDataRequests call including the email filter
 * */
class DataRequestQueryManagerTest {
    private lateinit var dataRequestQueryManager: DataRequestQueryManager
    private lateinit var mockDataRequestRepository: DataRequestRepository
    private lateinit var mockCompanyDataControllerApi: CompanyDataControllerApi
    private lateinit var mockDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var mockKeycloakUserService: KeycloakUserService
    private lateinit var mockDataRequestMasker: DataRequestMasker
    private lateinit var mockRequestPriorityAggregator: RequestPriorityAggregator
    private lateinit var mockAuthentication: DatalandJwtAuthentication
    private val userId = "1234-221-1111elf"

    private val dataRequestLogger = mock(DataRequestLogger::class.java)

    private val testCompanyId = UUID.randomUUID().toString()
    private val testReportingPeriod = "2024"
    private val testCompanyInformation =
        CompanyInformation(
            companyName = "Dummy Company",
            headquarters = "Berlin",
            identifiers = emptyMap(),
            countryCode = "DE",
            companyContactDetails = listOf("test@dummy.de"),
        )

    private val keycloakUserAlpha =
        KeycloakUserInfo(
            email = "alpha@fakemail.de",
            userId = UUID.randomUUID().toString(),
            firstName = "Michael",
            lastName = "Smith",
        )
    private val dataRequestEntityAlpha =
        DataRequestEntity(
            userId = keycloakUserAlpha.userId,
            dataType = DataTypeEnum.p2p.value,
            reportingPeriod = testReportingPeriod,
            creationTimestamp = Instant.now().toEpochMilli(),
            datalandCompanyId = testCompanyId,
        )

    private val keycloakUserBeta =
        KeycloakUserInfo(
            email = "beta@fakemail.de",
            userId = UUID.randomUUID().toString(),
            firstName = "Lisa",
            lastName = "Jackson",
        )
    private val dataRequestEntityBeta =
        DataRequestEntity(
            userId = keycloakUserBeta.userId,
            dataType = DataTypeEnum.lksg.value,
            reportingPeriod = testReportingPeriod,
            creationTimestamp = Instant.now().toEpochMilli(),
            datalandCompanyId = testCompanyId,
        )

    private val filterWithoutEmailAddress =
        DataRequestsFilter(
            setOf(DataTypeEnum.p2p, DataTypeEnum.lksg),
            null,
            null,
            testCompanyId,
            testReportingPeriod,
            setOf(RequestStatus.Open),
            null,
            null,
            null,
        )

    private val emailAddressSubstring = "beta"

    private val filterWithEmailAddressBeta =
        DataRequestsFilter(
            setOf(DataTypeEnum.p2p, DataTypeEnum.lksg),
            null,
            emailAddressSubstring,
            testCompanyId,
            testReportingPeriod,
            setOf(RequestStatus.Open),
            null,
            null,
            null,
        )

    private fun setupMocks() {
        mockDataRequestProcessingUtils = mock(DataRequestProcessingUtils::class.java)
        mockRequestPriorityAggregator = mock(RequestPriorityAggregator::class.java)

        mockCompanyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        `when`(mockCompanyDataControllerApi.getCompanyInfo(testCompanyId))
            .thenReturn(testCompanyInformation)

        mockDataRequestRepository = mock(DataRequestRepository::class.java)
        `when`(
            mockDataRequestRepository.searchDataRequestEntity(eq(filterWithoutEmailAddress), anyOrNull(), anyOrNull()),
        ).thenReturn(listOf(dataRequestEntityAlpha, dataRequestEntityBeta))
        `when`(
            mockDataRequestRepository.searchDataRequestEntity(eq(filterWithEmailAddressBeta), anyOrNull(), anyOrNull()),
        ).thenReturn(listOf(dataRequestEntityBeta))

        mockKeycloakUserService = mock(KeycloakUserService::class.java)
        `when`(
            mockKeycloakUserService.getUser(keycloakUserAlpha.userId),
        ).thenReturn(keycloakUserAlpha)
        `when`(
            mockKeycloakUserService.getUser(keycloakUserBeta.userId),
        ).thenReturn(keycloakUserBeta)
        `when`(
            mockKeycloakUserService.searchUsers(emailAddressSubstring),
        ).thenReturn(listOf(keycloakUserBeta))
    }

    private fun setupAdminAuthentication() {
        val mockSecurityContext = mock(SecurityContext::class.java)
        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "userEmail",
                userId,
                setOf(DatalandRealmRole.ROLE_ADMIN),
            )
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        `when`(mockAuthentication.credentials).thenReturn("")
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @BeforeEach
    fun setupDataRequestQueryManager() {
        setupMocks()
        mockDataRequestMasker = DataRequestMasker(mockKeycloakUserService)
        dataRequestQueryManager =
            DataRequestQueryManager(
                dataRequestRepository = mockDataRequestRepository,
                dataRequestLogger = dataRequestLogger,
                companyDataControllerApi = mockCompanyDataControllerApi,
                processingUtils = mockDataRequestProcessingUtils,
                keycloakUserControllerApiService = mockKeycloakUserService,
                dataRequestMasker = mockDataRequestMasker,
            )
    }

    @Test
    fun `simulate getDataRequests call without email filter `() {
        setupAdminAuthentication()
        val queryResults =
            dataRequestQueryManager.getDataRequests(
                emptyList(),
                filterWithoutEmailAddress,
                null,
                null,
            )

        verify(mockKeycloakUserService, times(0)).searchUsers(anyString())
        verify(mockKeycloakUserService, times(1)).getUser(keycloakUserAlpha.userId)
        verify(mockKeycloakUserService, times(1)).getUser(keycloakUserBeta.userId)
        assertEquals(2, queryResults!!.size)
        assertEquals(keycloakUserAlpha.email, queryResults[0].userEmailAddress)
        assertEquals(keycloakUserBeta.email, queryResults[1].userEmailAddress)
    }

    @Test
    fun `simulate getDataRequests call with email filter `() {
        setupAdminAuthentication()
        val queryResults =
            dataRequestQueryManager.getDataRequests(
                emptyList(),
                filterWithEmailAddressBeta,
                null,
                null,
            )

        verify(mockKeycloakUserService, times(2)).searchUsers(emailAddressSubstring)
        verify(mockKeycloakUserService, times(0)).getUser(anyString())
        assertEquals(1, queryResults!!.size)
        assertEquals(keycloakUserBeta.email, queryResults[0].userEmailAddress)
    }
}
