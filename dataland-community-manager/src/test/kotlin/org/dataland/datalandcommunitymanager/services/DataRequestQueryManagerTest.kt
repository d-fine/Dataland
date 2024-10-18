package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.KeycloakUserInfo
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
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
import java.time.Instant
import java.util.UUID

class DataRequestQueryManagerTest {
    private lateinit var dataRequestQueryManager: DataRequestQueryManager
    private lateinit var dataRequestRepository: DataRequestRepository
    private lateinit var companyDataControllerApi: CompanyDataControllerApi
    private lateinit var processingUtils: DataRequestProcessingUtils
    private lateinit var keycloakUserControllerApiService: KeycloakUserControllerApiService
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
        )

    private fun setupMocks() {
        processingUtils = mock(DataRequestProcessingUtils::class.java)

        companyDataControllerApi = mock(CompanyDataControllerApi::class.java)
        `when`(companyDataControllerApi.getCompanyInfo(testCompanyId))
            .thenReturn(testCompanyInformation)

        dataRequestRepository = mock(DataRequestRepository::class.java)
        `when`(
            dataRequestRepository.searchDataRequestEntity(eq(filterWithoutEmailAddress), anyOrNull(), anyOrNull()),
        ).thenReturn(listOf(dataRequestEntityAlpha, dataRequestEntityBeta))
        `when`(
            dataRequestRepository.searchDataRequestEntity(eq(filterWithEmailAddressBeta), anyOrNull(), anyOrNull()),
        ).thenReturn(listOf(dataRequestEntityBeta))

        keycloakUserControllerApiService = mock(KeycloakUserControllerApiService::class.java)
        `when`(
            keycloakUserControllerApiService.getUser(keycloakUserAlpha.userId),
        ).thenReturn(keycloakUserAlpha)
        `when`(
            keycloakUserControllerApiService.getUser(keycloakUserBeta.userId),
        ).thenReturn(keycloakUserBeta)
        `when`(
            keycloakUserControllerApiService.searchUsers(emailAddressSubstring),
        ).thenReturn(listOf(keycloakUserBeta))
    }

    @BeforeEach
    fun setupDataRequestQueryManager() {
        setupMocks()
        dataRequestQueryManager =
            DataRequestQueryManager(
                dataRequestRepository = dataRequestRepository,
                dataRequestLogger = dataRequestLogger,
                companyDataControllerApi = companyDataControllerApi,
                processingUtils = processingUtils,
                keycloakUserControllerApiService = keycloakUserControllerApiService,
            )
    }

    @Test
    fun `simulate getDataRequests call without email filter `() {
        val queryResults =
            dataRequestQueryManager.getDataRequests(
                true,
                emptyList(),
                filterWithoutEmailAddress,
                null,
                null,
            )

        verify(keycloakUserControllerApiService, times(0)).searchUsers(anyString())
        verify(keycloakUserControllerApiService, times(1)).getUser(keycloakUserAlpha.userId)
        verify(keycloakUserControllerApiService, times(1)).getUser(keycloakUserBeta.userId)
        assertEquals(2, queryResults!!.size)
        assertEquals(keycloakUserAlpha.email, queryResults[0].userEmailAddress)
        assertEquals(keycloakUserBeta.email, queryResults[1].userEmailAddress)
    }

    @Test
    fun `simulate getDataRequests call with email filter `() {
        val queryResults =
            dataRequestQueryManager.getDataRequests(
                true,
                emptyList(),
                filterWithEmailAddressBeta,
                null,
                null,
            )

        verify(keycloakUserControllerApiService, times(1)).searchUsers(emailAddressSubstring)
        verify(keycloakUserControllerApiService, times(0)).getUser(anyString())
        assertEquals(1, queryResults!!.size)
        assertEquals(keycloakUserBeta.email, queryResults[0].userEmailAddress)
    }
}
