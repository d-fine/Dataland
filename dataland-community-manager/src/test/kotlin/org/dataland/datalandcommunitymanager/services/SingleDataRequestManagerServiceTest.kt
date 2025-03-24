package org.dataland.datalandcommunitymanager.services

import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.services.KeycloakUserService
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandcommunitymanager.repositories.DataRequestRepository
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandcommunitymanager.utils.DataRequestLogger
import org.dataland.datalandcommunitymanager.utils.DataRequestProcessingUtils
import org.dataland.datalandcommunitymanager.utils.DataRequestsFilter
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class SingleDataRequestManagerServiceTest(
    @Autowired val dataRequestLogger: DataRequestLogger,
    @Autowired val dataRequestProcessingUtils: DataRequestProcessingUtils,
    @Autowired val accessRequestEmailSender: AccessRequestEmailSender,
    @Autowired val dataRequestRepository: DataRequestRepository,
    @Autowired val securityUtilsService: SecurityUtilsService,
    @Autowired val companyRolesManager: CompanyRolesManager,
) {
    @MockitoBean
    private val mockCompanyInfoService = mock<CompanyInfoService>()

    @MockitoBean
    private val mockDataAccessManager = mock<DataAccessManager>()

    @MockitoBean
    private val mockSingleDataRequestEmailMessageSender = mock<SingleDataRequestEmailMessageSender>()

    @MockitoBean
    private val mockKeycloakUserService = mock<KeycloakUserService>()

    private lateinit var singleDataRequestManager: SingleDataRequestManager
    private lateinit var spyDataRequestProcessingUtils: DataRequestProcessingUtils
    private lateinit var mockAuthentication: DatalandAuthentication

    private val mockSecurityContext = mock<SecurityContext>()
    private val dummyCompanyId = "00000000-0000-0000-0000-000000000000"
    private val sampleDataType = DataTypeEnum.eutaxonomyMinusFinancials
    private val sampleReportingPeriod = "2023"
    private val sampleReportingPeriods = setOf(sampleReportingPeriod)
    private val dummySingleDataRequest =
        SingleDataRequest(
            companyIdentifier = dummyCompanyId,
            dataType = sampleDataType,
            reportingPeriods = sampleReportingPeriods,
            contacts = null,
            message = null,
        )
    private val dummyUserId = "user-id"
    private val adminUserId = "admin-user-id"
    private val adminUserName = "data_admin"

    @BeforeEach
    fun setUp() {
        spyDataRequestProcessingUtils = spy(dataRequestProcessingUtils)

        reset(
            mockCompanyInfoService,
            mockDataAccessManager,
            mockSingleDataRequestEmailMessageSender,
            mockSecurityContext,
            mockKeycloakUserService,
        )

        // The following method is only used to check for existing datasets in the context of
        // access requests.
        doReturn(false).whenever(spyDataRequestProcessingUtils).matchingDatasetExists(
            anyString(), anyString(), any(),
        )
        doReturn(false).whenever(mockDataAccessManager).hasAccessToPrivateDataset(
            anyString(), anyString(), anyOrNull(), anyString(),
        )
        doReturn(false).whenever(mockDataAccessManager).existsAccessRequestWithNonPendingStatus(
            anyString(), anyOrNull(), anyString(), anyString(),
        )
        doReturn(listOf("ROLE_ADMIN", "ROLE_PREMIUM_USER"))
            .whenever(mockKeycloakUserService)
            .getUserRoleNames(eq(adminUserId))
        doReturn(listOf("ROLE_USER"))
            .whenever(mockKeycloakUserService)
            .getUserRoleNames(eq(dummyUserId))

        singleDataRequestManager =
            SingleDataRequestManager(
                dataRequestLogger = dataRequestLogger,
                dataRequestRepository = dataRequestRepository,
                companyInfoService = mockCompanyInfoService,
                singleDataRequestEmailMessageSender = mockSingleDataRequestEmailMessageSender,
                utils = spyDataRequestProcessingUtils,
                dataAccessManager = mockDataAccessManager,
                accessRequestEmailSender = accessRequestEmailSender,
                securityUtilsService = securityUtilsService,
                companyRolesManager = companyRolesManager,
                keycloakUserService = mockKeycloakUserService,
                maxRequestsForUser = 10,
            )

        mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                username = adminUserName,
                userId = adminUserId,
                roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_PREMIUM_USER),
            )
        doReturn(mockAuthentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `check that an impersonated request is saved correctly in the database`() {
        singleDataRequestManager.processSingleDataRequest(
            dummySingleDataRequest,
            dummyUserId,
        )

        val searchResults = dataRequestRepository.findByUserId(dummyUserId)

        assertEquals(searchResults.size, 1)

        val savedEntity = searchResults[0]

        assertEquals(savedEntity.userId, dummyUserId)
        assertEquals(savedEntity.dataType, sampleDataType.toString())
        assertEquals(savedEntity.reportingPeriod, sampleReportingPeriod)
        assertEquals(savedEntity.datalandCompanyId, dummyCompanyId)
    }

    @Test
    fun `check that admins can create impersonation requests after posting their own request for a dataset`() {
        singleDataRequestManager.processSingleDataRequest(dummySingleDataRequest)

        singleDataRequestManager.processSingleDataRequest(
            dummySingleDataRequest,
            dummyUserId,
        )

        val searchFilter =
            DataRequestsFilter(
                dataType = setOf(sampleDataType),
                datalandCompanyIds = setOf(dummyCompanyId),
                reportingPeriod = sampleReportingPeriod,
            )

        val searchResults = dataRequestRepository.searchDataRequestEntity(searchFilter)

        assertEquals(searchResults.size, 2)

        val expectedAdminRequest: DataRequestEntity
        val expectedImpersonatedRequest: DataRequestEntity

        if (searchResults[0].userId == dummyUserId) {
            expectedImpersonatedRequest = searchResults[0]
            expectedAdminRequest = searchResults[1]
        } else {
            expectedAdminRequest = searchResults[0]
            expectedImpersonatedRequest = searchResults[1]
        }

        assertEquals(expectedAdminRequest.userId, adminUserId)
        assertEquals(expectedImpersonatedRequest.userId, dummyUserId)
    }
}
