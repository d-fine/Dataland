package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.BasicCompanyInformation
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandcommunitymanager.entities.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.exceptions.DataRequestNotFoundApiException
import org.dataland.datalandcommunitymanager.model.dataRequest.ExtendedStoredDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.StoredDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.CommunityManagerDataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestMasker
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
import java.io.File
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
    private val mockCommunityManagerDataRequestProcessingUtils = mock<CommunityManagerDataRequestProcessingUtils>()
    private val mockKeycloakUserService = mock<KeycloakUserService>()
    private val mockDataRequestMasker = mock<DataRequestMasker>()

    private val objectMapper = JsonUtils.defaultObjectMapper

    private val userId = "1234-221-1111elf"

    private val testCompanyInformationAlpha =
        objectMapper.readValue<CompanyInformation>(
            File("./src/test/resources/dataRequestQueryManager/testCompanyInformationAlpha.json"),
        )
    private val keycloakUserAlpha =
        objectMapper.readValue<KeycloakUserInfo>(
            File("./src/test/resources/dataRequestQueryManager/keycloakUserAlpha.json"),
        )
    private val testBasicCompanyInformationAlpha =
        objectMapper.readValue<BasicCompanyInformation>(
            File("./src/test/resources/dataRequestQueryManager/testBasicCompanyInformationAlpha.json"),
        )
    private val dataRequestEntityAlpha =
        objectMapper.readValue<DataRequestEntity>(
            File("./src/test/resources/dataRequestQueryManager/dataRequestEntityAlpha.json"),
        )

    private val testCompanyInformationBeta =
        objectMapper.readValue<CompanyInformation>(
            File("./src/test/resources/dataRequestQueryManager/testCompanyInformationBeta.json"),
        )
    private val keycloakUserBeta =
        objectMapper.readValue<KeycloakUserInfo>(
            File("./src/test/resources/dataRequestQueryManager/keycloakUserBeta.json"),
        )
    private val dataRequestEntityBeta =
        objectMapper.readValue<DataRequestEntity>(
            File("./src/test/resources/dataRequestQueryManager/dataRequestEntityBeta.json"),
        )

    private val testCompanyIdAlpha = testBasicCompanyInformationAlpha.companyId
    private val testCompanyIdBeta = dataRequestEntityBeta.datalandCompanyId
    private val companySearchString = testCompanyInformationAlpha.companyName.substring(0, 3).lowercase()
    private val emailAddressSubstring = keycloakUserBeta.email!!.substring(0, 4)

    private val userIdsToEmailAddresses =
        mapOf(
            keycloakUserAlpha.userId to keycloakUserAlpha.email,
            keycloakUserBeta.userId to keycloakUserBeta.email,
        )

    private val nonexistentDataRequestId = UUID.randomUUID().toString()

    private val filterWithoutEmailAddress =
        DataRequestsFilter(
            dataType =
                setOf(
                    DataTypeEnum.decode(dataRequestEntityAlpha.dataType)!!,
                    DataTypeEnum.decode(dataRequestEntityBeta.dataType)!!,
                ),
            datalandCompanyIds = setOf(testCompanyIdAlpha, testCompanyIdBeta),
            reportingPeriods =
                setOf(
                    dataRequestEntityAlpha.reportingPeriod,
                    dataRequestEntityBeta.reportingPeriod,
                ),
            requestStatus =
                setOf(
                    dataRequestEntityAlpha.requestStatus,
                    dataRequestEntityBeta.requestStatus,
                ),
        )

    private val filterWithEmailAddressBeta =
        filterWithoutEmailAddress.copy(
            emailAddress = emailAddressSubstring,
        )

    private val dummyAggregatedRequests =
        listOf(
            object : AggregatedDataRequest {
                override val dataType: String = "sfdr"
                override val reportingPeriod: String = "2023"
                override val datalandCompanyId: String = testCompanyIdAlpha
                override val priority: String = RequestPriority.High.toString()
                override val requestStatus: String = RequestStatus.Open.toString()
                override val count: Long = 1
            },
            object : AggregatedDataRequest {
                override val dataType: String = "sfdr"
                override val reportingPeriod: String = "2023"
                override val datalandCompanyId: String = testCompanyIdAlpha
                override val priority: String = RequestPriority.Low.toString()
                override val requestStatus: String = RequestStatus.Open.toString()
                override val count: Long = 0
            },
        )

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
        doAnswer { invocation ->
            val companyIds = invocation.getArgument<List<String>?>(1)
            listOf(dataRequestEntityAlpha, dataRequestEntityBeta)
                .filter {
                    companyIds == null || it.datalandCompanyId in companyIds
                }.size
        }.whenever(mockDataRequestRepository).getNumberOfRequests(eq(filterWithoutEmailAddress), anyOrNull())

        doReturn(listOf(dataRequestEntityBeta))
            .whenever(mockDataRequestRepository)
            .searchDataRequestEntity(eq(filterWithEmailAddressBeta), anyOrNull(), anyOrNull(), anyOrNull())
        doReturn(1).whenever(mockDataRequestRepository).getNumberOfRequests(eq(filterWithEmailAddressBeta), anyOrNull())

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

        doReturn(DataTypeEnum.sfdr).whenever(mockCommunityManagerDataRequestProcessingUtils).getDataTypeEnumForFrameworkName("sfdr")
    }

    @BeforeEach
    fun setupDataRequestQueryManager() {
        TestUtils.mockSecurityContext("userEmail", userId, DatalandRealmRole.ROLE_ADMIN)
        setupMocks()
        dataRequestQueryManager =
            DataRequestQueryManager(
                dataRequestRepository = mockDataRequestRepository,
                dataRequestLogger = mockDataRequestLogger,
                companyDataControllerApi = mockCompanyDataControllerApi,
                processingUtils = mockCommunityManagerDataRequestProcessingUtils,
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
        val numberOfResults =
            dataRequestQueryManager.getNumberOfDataRequests(
                filterWithoutEmailAddress,
                null,
            )

        verify(mockKeycloakUserService, times(0)).searchUsers(any())
        assertEquals(2, queryResults!!.size)
        assertEquals(2, numberOfResults)
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
        val numberOfResults =
            dataRequestQueryManager.getNumberOfDataRequests(
                filterWithEmailAddressBeta,
                null,
            )

        verify(mockKeycloakUserService, times(2)).searchUsers(emailAddressSubstring)
        verify(mockKeycloakUserService, times(0)).getUser(any())
        assertEquals(1, queryResults!!.size)
        assertEquals(1, numberOfResults)
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
        val numberOfResults =
            dataRequestQueryManager.getNumberOfDataRequests(
                filterWithoutEmailAddress,
                companySearchString,
            )

        verify(mockCompanyDataControllerApi, times(2))
            .getCompanies(searchString = companySearchString, chunkIndex = 0, chunkSize = Int.MAX_VALUE)
        assertEquals(1, queryResults!!.size)
        assertEquals(1, numberOfResults)
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
