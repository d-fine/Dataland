package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestMasker
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import java.time.Instant
import java.util.Optional
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
    private lateinit var dataRequestEntityAlpha: DataRequestEntity

    private val keycloakUserBeta =
        KeycloakUserInfo(
            email = "beta@fakemail.de",
            userId = UUID.randomUUID().toString(),
            firstName = "Lisa",
            lastName = "Jackson",
        )
    private lateinit var dataRequestEntityBeta: DataRequestEntity

    private val filterWithoutEmailAddress =
        DataRequestsFilter(
            dataType = setOf(DataTypeEnum.sfdr, DataTypeEnum.lksg),
            datalandCompanyIds = setOf(testCompanyId),
            reportingPeriod = testReportingPeriod,
            requestStatus = setOf(RequestStatus.Open),
        )

    private val emailAddressSubstring = "beta"

    private val filterWithEmailAddressBeta =
        DataRequestsFilter(
            dataType = setOf(DataTypeEnum.sfdr, DataTypeEnum.lksg),
            emailAddress = emailAddressSubstring,
            datalandCompanyIds = setOf(testCompanyId),
            reportingPeriod = testReportingPeriod,
            requestStatus = setOf(RequestStatus.Open),
        )

    private val dummyAggregatedRequests =
        listOf(
            object : AggregatedDataRequest {
                override val dataType: String = "sfdr"
                override val reportingPeriod: String = "2023"
                override val datalandCompanyId: String = testCompanyId
                override val priority: String = RequestPriority.Low.toString()
                override val requestStatus: String = RequestStatus.Open.toString()
                override val count: Long = 1
            },
            object : AggregatedDataRequest {
                override val dataType: String = "sfdr"
                override val reportingPeriod: String = "2023"
                override val datalandCompanyId: String = testCompanyId
                override val priority: String = RequestPriority.High.toString()
                override val requestStatus: String = RequestStatus.Open.toString()
                override val count: Long = 0
            },
        )

    private fun setupDataRequestEntities() {
        dataRequestEntityAlpha =
            DataRequestEntity(
                userId = keycloakUserAlpha.userId,
                dataType = DataTypeEnum.sfdr.value,
                notifyMeImmediately = false,
                reportingPeriod = testReportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                datalandCompanyId = testCompanyId,
            )
        dataRequestEntityBeta =
            DataRequestEntity(
                userId = keycloakUserBeta.userId,
                dataType = DataTypeEnum.lksg.value,
                notifyMeImmediately = false,
                reportingPeriod = testReportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                datalandCompanyId = testCompanyId,
            )
    }

    private fun setupMocks() {
        mockDataRequestProcessingUtils = mock(DataRequestProcessingUtils::class.java)

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
        `when`(
            mockDataRequestRepository.findByUserId(eq(dataRequestEntityAlpha.userId)),
        ).thenReturn(listOf(dataRequestEntityAlpha))
        `when`(
            mockDataRequestRepository.findByUserId(eq(dataRequestEntityBeta.userId)),
        ).thenReturn(listOf(dataRequestEntityBeta))
        `when`(
            mockDataRequestRepository.fetchStatusHistory(eq(listOf(dataRequestEntityAlpha))),
        ).thenReturn(listOf(dataRequestEntityAlpha))
        `when`(
            mockDataRequestRepository.fetchStatusHistory(eq(listOf(dataRequestEntityBeta))),
        ).thenReturn(listOf(dataRequestEntityBeta))
        `when`(
            mockDataRequestRepository.findById(eq(dataRequestEntityAlpha.dataRequestId)),
        ).thenReturn(Optional.empty())
        `when`(
            mockDataRequestRepository.findById(eq(dataRequestEntityBeta.dataRequestId)),
        ).thenReturn(Optional.of(dataRequestEntityBeta))
        `when`(
            mockDataRequestRepository.getAggregatedDataRequests(any()),
        ).thenReturn(dummyAggregatedRequests)

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

        `when`(
            mockDataRequestProcessingUtils.getDataTypeEnumForFrameworkName("sfdr"),
        ).thenReturn(DataTypeEnum.sfdr)
    }

    @BeforeEach
    fun setupDataRequestQueryManager() {
        TestUtils.mockSecurityContext("userEmail", userId, DatalandRealmRole.ROLE_ADMIN)
        setupDataRequestEntities()
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
                requestPriorityAggregator = RequestPriorityAggregator(),
            )
    }

    @Test
    fun `simulate getDataRequests call without email filter `() {
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

    @Test
    fun `simulate getDataRequestsForRequestingUser call `() {
        TestUtils.mockSecurityContext(
            keycloakUserAlpha.email ?: "userEmail",
            keycloakUserAlpha.userId,
            DatalandRealmRole.ROLE_USER,
        )
        val queryResults = dataRequestQueryManager.getDataRequestsForRequestingUser()

        verify(mockDataRequestRepository, times(1)).findByUserId(keycloakUserAlpha.userId)
        verify(mockDataRequestRepository, times(1)).fetchStatusHistory(any())
        verify(mockCompanyDataControllerApi, times(1)).getCompanyInfo(dataRequestEntityAlpha.datalandCompanyId)
        assertEquals(1, queryResults.size)
        assertEquals(testCompanyInformation.companyName, queryResults[0].companyName)
    }

    @Test
    fun `simulate getDataRequestsById call with valid Id `() {
        val dataRequest = dataRequestQueryManager.getDataRequestById(dataRequestEntityBeta.dataRequestId)

        verify(mockDataRequestRepository, times(1)).findById(dataRequestEntityBeta.dataRequestId)
        verify(mockKeycloakUserService, times(1)).getUser(anyString())
        assertEquals(keycloakUserBeta.email, dataRequest.userEmailAddress)
        assertEquals(dataRequestEntityBeta.dataRequestId, dataRequest.dataRequestId)
    }

    @Test
    fun `simulate getDataRequestsById call with invalid Id `() {
        assertThrows<DataRequestNotFoundApiException> {
            dataRequestQueryManager.getDataRequestById(dataRequestEntityAlpha.dataRequestId)
        }

        verify(mockDataRequestRepository, times(1)).findById(dataRequestEntityAlpha.dataRequestId)
        verify(mockKeycloakUserService, times(0)).getUser(anyString())
    }

    @Test
    fun `simulate getAggregatedOpenDataRequestsWithAggregatedRequestPriority call `() {
        val aggregatedDataRequests =
            dataRequestQueryManager.getAggregatedOpenDataRequestsWithAggregatedRequestPriority(
                setOf(DataTypeEnum.sfdr), "2023", null,
            )

        verify(mockDataRequestRepository, times(1)).getAggregatedDataRequests(any())
        assertEquals(1, aggregatedDataRequests.size)
    }
}
