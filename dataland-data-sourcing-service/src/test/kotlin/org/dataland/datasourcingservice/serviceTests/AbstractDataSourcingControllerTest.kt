package org.dataland.datasourcingservice.serviceTests

import okhttp3.OkHttpClient
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRoleAssignmentExtended
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datasourcingservice.DatalandDataSourcingService
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.dataland.datasourcingservice.services.DataSourcingValidator
import org.dataland.datasourcingservice.utils.DataBaseCreationUtils
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
abstract class AbstractDataSourcingControllerTest {
    @Autowired
    protected lateinit var dataSourcingRepository: DataSourcingRepository

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @MockitoBean
    protected lateinit var mockCompanyRolesControllerApi: CompanyRolesControllerApi

    @MockitoBean
    protected lateinit var mockDataSourcingValidator: DataSourcingValidator

    @MockitoBean
    protected lateinit var mockCloudEventMessageHandler: CloudEventMessageHandler

    @MockitoBean
    @Qualifier("AuthenticatedOkHttpClient")
    protected lateinit var authenticatedOkHttpClient: OkHttpClient

    protected val dataBaseCreationUtils: DataBaseCreationUtils by lazy {
        DataBaseCreationUtils(dataSourcingRepository = dataSourcingRepository)
    }

    protected val mockSecurityContext: SecurityContext = mock()

    protected val adminUserId: UUID = UUID.randomUUID()
    protected val regularUserId: UUID = UUID.randomUUID()

    protected val dummyAdminAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_ADMIN",
            userId = adminUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_USER),
        )

    protected val dummyUserAuthentication: DatalandJwtAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_USER",
            userId = regularUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    protected val dataSourcingId: UUID = UUID.randomUUID()
    protected val documentCollectorId: UUID = UUID.randomUUID()
    protected val dataExtractorId: UUID = UUID.randomUUID()

    protected val memberAssignmentForDocumentCollector: CompanyRoleAssignmentExtended =
        CompanyRoleAssignmentExtended(
            companyRole = CompanyRole.Analyst,
            userId = regularUserId.toString(),
            companyId = documentCollectorId.toString(),
            email = "test@example.com",
            firstName = "Jane",
            lastName = "Doe",
        )

    protected val memberAssignmentForDataExtractor: CompanyRoleAssignmentExtended =
        CompanyRoleAssignmentExtended(
            companyRole = CompanyRole.Analyst,
            userId = regularUserId.toString(),
            companyId = dataExtractorId.toString(),
            email = "test@example.com",
            firstName = "Jane",
            lastName = "Doe",
        )

    @BeforeEach
    fun setupBase() {
        reset(
            mockSecurityContext,
            mockCompanyRolesControllerApi,
            mockDataSourcingValidator,
            mockCloudEventMessageHandler,
            authenticatedOkHttpClient,
        )
        stubRoleAssignments(adminUserId, documentCollectorId, emptyList())
        dataBaseCreationUtils.storeDataSourcing(
            dataSourcingId = dataSourcingId,
            state = DataSourcingState.DocumentSourcing,
            documentCollector = documentCollectorId,
            dataExtractor = dataExtractorId,
        )
    }

    @AfterEach
    fun cleanup() {
        dataSourcingRepository.deleteAll()
    }

    protected fun setMockSecurityContext(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    protected fun stubRoleAssignments(
        userId: UUID,
        companyId: UUID,
        roles: List<CompanyRoleAssignmentExtended>,
    ) {
        doReturn(roles)
            .whenever(mockCompanyRolesControllerApi)
            .getExtendedCompanyRoleAssignments(userId = userId, companyId = companyId)
    }
}
