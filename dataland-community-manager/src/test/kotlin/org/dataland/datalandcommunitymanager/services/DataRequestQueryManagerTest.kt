package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
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
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional
import java.util.UUID

/**
 * Tests the getDataRequests call including the email filter
 * */
class DataRequestQueryManagerTest {
    private lateinit var dataRequestQueryManager: DataRequestQueryManager
    private val mockDataRequestRepository = mock<DataRequestRepository>()
    private val mockDataRequestLogger = mock<DataRequestLogger>()
    private val mockCompanyDataControllerApi = mock<CompanyDataControllerApi>()
    private val mockDataRequestProcessingUtils = mock<DataRequestProcessingUtils>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val mockDataRequestMasker = mock<DataRequestMasker>()

    private val userId = "1234-221-1111elf"

    private val testCompanyIdAlpha = UUID.randomUUID().toString()
    private val testCompanyIdBeta = UUID.randomUUID().toString()
    private val testReportingPeriod = "2024"

    private val companySearchString = "dum"
    private val companyNameAlpha = "Dummy Company"
    private val companyHeadquartersAlpha = "Berlin"
    private val companyCountryCodeAlpha = "DE"

    private val testCompanyInformationAlpha =
        CompanyInformation(
            companyName = companyNameAlpha,
            headquarters = companyHeadquartersAlpha,
            identifiers = emptyMap(),
            countryCode = companyCountryCodeAlpha,
            companyContactDetails = listOf("test@dummy.de"),
        )

    private val keycloakUserAlpha =
        KeycloakUserInfo(
            email = "alpha@fakemail.de",
            userId = UUID.randomUUID().toString(),
            firstName = "Michael",
            lastName = "Smith",
        )

    private val testBasicCompanyInformationAlpha =
        BasicCompanyInformation(
            companyId = testCompanyIdAlpha,
            companyName = companyNameAlpha,
            headquarters = companyHeadquartersAlpha,
            countryCode = companyCountryCodeAlpha,
        )

    private lateinit var dataRequestEntityAlpha: DataRequestEntity

    private val keycloakUserBeta =
        KeycloakUserInfo(
            email = "beta@fakemail.de",
            userId = UUID.randomUUID().toString(),
            firstName = "Lisa",
            lastName = "Jackson",
        )

    private val testCompanyInformationBeta =
        CompanyInformation(
            companyName = "Fantasy company",
            headquarters = "Wien",
            identifiers = emptyMap(),
            countryCode = "AT",
            companyContactDetails = listOf("test@fantasy.at"),
        )

    private lateinit var dataRequestEntityBeta: DataRequestEntity

    private val nonexistentDataRequestId = UUID.randomUUID().toString()

    private val userIdsToEmailAddresses =
        mapOf(
            keycloakUserAlpha.userId to keycloakUserAlpha.email,
            keycloakUserBeta.userId to keycloakUserBeta.email,
        )

    private val filterWithoutEmailAddress =
        DataRequestsFilter(
            dataType = setOf(DataTypeEnum.sfdr, DataTypeEnum.lksg),
            datalandCompanyIds = setOf(testCompanyIdAlpha, testCompanyIdBeta),
            reportingPeriods = setOf(testReportingPeriod),
            requestStatus = setOf(RequestStatus.Open),
        )

    private val emailAddressSubstring = "beta"

    private val filterWithEmailAddressBeta =
        DataRequestsFilter(
            dataType = setOf(DataTypeEnum.sfdr, DataTypeEnum.lksg),
            emailAddress = emailAddressSubstring,
            datalandCompanyIds = setOf(testCompanyIdAlpha, testCompanyIdBeta),
            reportingPeriods = setOf(testReportingPeriod),
            requestStatus = setOf(RequestStatus.Open),
        )

    private val dummyAggregatedRequests =
        listOf(
            object : AggregatedDataRequest {
                override val dataType: String = "sfdr"
                override val reportingPeriod: String = "2023"
                override val datalandCompanyId: String = testCompanyIdAlpha
                override val priority: String = RequestPriority.Low.toString()
                override val requestStatus: String = RequestStatus.Open.toString()
                override val count: Long = 1
            },
            object : AggregatedDataRequest {
                override val dataType: String = "sfdr"
                override val reportingPeriod: String = "2023"
                override val datalandCompanyId: String = testCompanyIdAlpha
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
                datalandCompanyId = testCompanyIdAlpha,
            )
        dataRequestEntityBeta =
            DataRequestEntity(
                userId = keycloakUserBeta.userId,
                dataType = DataTypeEnum.lksg.value,
                notifyMeImmediately = false,
                reportingPeriod = testReportingPeriod,
                creationTimestamp = Instant.now().toEpochMilli(),
                datalandCompanyId = testCompanyIdBeta,
            )
    }

    private fun setupMocks() {
        doReturn(testCompanyInformationAlpha).whenever(mockCompanyDataControllerApi).getCompanyInfo(testCompanyIdAlpha)

        doReturn(testCompanyInformationBeta).whenever(mockCompanyDataControllerApi).getCompanyInfo(testCompanyIdBeta)

        doReturn(listOf(testBasicCompanyInformationAlpha))
            .whenever(mockCompanyDataControllerApi)
            .getCompanies(
                searchString = companySearchString, chunkIndex = 0,
                chunkSize = Int.MAX_VALUE,
            )

        doAnswer { invocation ->
            val companyIds = invocation.getArgument<List<String>?>(1)
            listOf(dataRequestEntityAlpha, dataRequestEntityBeta).filter { companyIds == null || it.datalandCompanyId in companyIds }
        }.whenever(mockDataRequestRepository)
            .searchDataRequestEntity(eq(filterWithoutEmailAddress), anyOrNull(), anyOrNull(), anyOrNull())

        doReturn(listOf(dataRequestEntityBeta))
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(eq(filterWithEmailAddressBeta), anyOrNull(), anyOrNull(), anyOrNull())

        doReturn(listOf(dataRequestEntityAlpha)).whenever(mockDataRequestRepository).findByUserId(eq(dataRequestEntityAlpha.userId))

        doReturn(listOf(dataRequestEntityBeta)).whenever(mockDataRequestRepository).findByUserId(eq(dataRequestEntityBeta.userId))

        doReturn(listOf(dataRequestEntityAlpha)).whenever(mockDataRequestRepository).fetchStatusHistory(eq(listOf(dataRequestEntityAlpha)))

        doReturn(listOf(dataRequestEntityBeta)).whenever(mockDataRequestRepository).fetchStatusHistory(eq(listOf(dataRequestEntityBeta)))

        doReturn(Optional.empty<DataRequestEntity>()).whenever(mockDataRequestRepository).findById(eq(nonexistentDataRequestId))

        doReturn(Optional.of(dataRequestEntityBeta)).whenever(mockDataRequestRepository).findById(eq(dataRequestEntityBeta.dataRequestId))

        doReturn(dummyAggregatedRequests).whenever(mockDataRequestRepository).getAggregatedDataRequests(any())

        doReturn(keycloakUserAlpha).whenever(mockKeycloakUserService).getUser(keycloakUserAlpha.userId)

        doReturn(keycloakUserBeta).whenever(mockKeycloakUserService).getUser(keycloakUserBeta.userId)

        doReturn(listOf(keycloakUserBeta)).whenever(mockKeycloakUserService).searchUsers(emailAddressSubstring)

        doAnswer { invocation ->
            val extendedStoredDataRequests = invocation.getArgument<List<ExtendedStoredDataRequest>>(0)
            extendedStoredDataRequests.map { it.copy(userEmailAddress = userIdsToEmailAddresses[it.userId]) }
        }.whenever(mockDataRequestMasker).addEmailAddressIfAllowedToSee(any(), any(), any())

        doAnswer { invocation ->
            invocation.getArgument<List<ExtendedStoredDataRequest>>(0)
        }.whenever(mockDataRequestMasker).hideAdminCommentForNonAdmins(any<List<ExtendedStoredDataRequest>>())

        doAnswer { it.getArgument<StoredDataRequest>(0) }
            .whenever(mockDataRequestMasker)
            .hideAdminCommentForNonAdmins(any<StoredDataRequest>())

        doReturn(DataTypeEnum.sfdr).whenever(mockDataRequestProcessingUtils).getDataTypeEnumForFrameworkName("sfdr")
    }

    @BeforeEach
    fun setupDataRequestQueryManager() {
        TestUtils.mockSecurityContext("userEmail", userId, DatalandRealmRole.ROLE_ADMIN)
        setupDataRequestEntities()
        setupMocks()
        dataRequestQueryManager =
            DataRequestQueryManager(
                dataRequestRepository = mockDataRequestRepository,
                dataRequestLogger = mockDataRequestLogger,
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
                null,
            )

        verify(mockKeycloakUserService, times(0)).searchUsers(any())
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
                null,
            )

        verify(mockKeycloakUserService, times(1)).searchUsers(emailAddressSubstring)
        verify(mockKeycloakUserService, times(0)).getUser(any())
        assertEquals(1, queryResults!!.size)
        assertEquals(keycloakUserBeta.email, queryResults[0].userEmailAddress)
    }

    @Test
    fun `simulate getDataRequests call with search string `() {
        val queryResults =
            dataRequestQueryManager.getDataRequests(
                emptyList(),
                filterWithoutEmailAddress,
                companySearchString,
                null,
                null,
            )

        verify(mockCompanyDataControllerApi, times(1))
            .getCompanies(searchString = companySearchString, chunkIndex = 0, chunkSize = Int.MAX_VALUE)
        assertEquals(1, queryResults!!.size)
        assertEquals(testCompanyIdAlpha, queryResults[0].datalandCompanyId)
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
        assertEquals(testCompanyInformationAlpha.companyName, queryResults[0].companyName)
    }

    @Test
    fun `simulate getDataRequestsById call with valid Id `() {
        val dataRequest = dataRequestQueryManager.getDataRequestById(dataRequestEntityBeta.dataRequestId)

        verify(mockDataRequestRepository, times(1)).findById(dataRequestEntityBeta.dataRequestId)
        verify(mockKeycloakUserService, times(1)).getUser(any())
        assertEquals(keycloakUserBeta.email, dataRequest.userEmailAddress)
        assertEquals(dataRequestEntityBeta.dataRequestId, dataRequest.dataRequestId)
    }

    @Test
    fun `simulate getDataRequestsById call with invalid Id `() {
        assertThrows<DataRequestNotFoundApiException> {
            dataRequestQueryManager.getDataRequestById(nonexistentDataRequestId)
        }

        verify(mockDataRequestRepository, times(1)).findById(nonexistentDataRequestId)
        verify(mockKeycloakUserService, times(0)).getUser(any())
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
