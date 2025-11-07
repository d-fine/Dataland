package org.dataland.datasourcingservice.serviceTests

import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRolesControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRoleAssignmentExtended
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
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
class DataSourcingControllerTest(
    @Autowired private val dataSourcingRepository: DataSourcingRepository,
) {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var mockCompanyRolesControllerApi: CompanyRolesControllerApi

    @MockitoBean
    private lateinit var mockDataSourcingValidator: DataSourcingValidator

    private val dataBaseCreationUtils = DataBaseCreationUtils(dataSourcingRepository = dataSourcingRepository)

    private val mockSecurityContext = mock<SecurityContext>()

    private val adminUserId = UUID.randomUUID()

    private val dummyAdminAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_ADMIN",
            userId = adminUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_ADMIN),
        )

    private val regularUserId = UUID.randomUUID()

    private val dummyUserAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_USER",
            userId = regularUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    private val dataSourcingId = UUID.randomUUID()
    private val documentCollectorId = UUID.randomUUID()
    private val documentId = "my-document-hash"

    @BeforeEach
    fun setup() {
        reset(
            mockSecurityContext,
            mockCompanyRolesControllerApi,
            mockDataSourcingValidator,
        )

        doReturn(emptyList<CompanyRoleAssignmentExtended>()).whenever(mockCompanyRolesControllerApi).getExtendedCompanyRoleAssignments(
            userId = adminUserId,
            companyId = documentCollectorId,
        )

        dataBaseCreationUtils.storeDataSourcing(
            dataSourcingId = dataSourcingId,
            state = DataSourcingState.DocumentSourcing,
            documentCollector = documentCollectorId,
        )
    }

    @AfterEach
    fun cleanup() {
        dataSourcingRepository.deleteAll()
    }

    private fun setMockSecurityContext(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun performPatchDocumentsRequestAndExpect(resultMatcher: ResultMatcher) {
        mockMvc
            .perform(
                patch("/data-sourcing/$dataSourcingId/documents")
                    .content("[ \"$documentId\" ]")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(resultMatcher)
    }

    private val companyRoleAssignmentForRegularUserInDocumentCollector =
        CompanyRoleAssignmentExtended(
            companyRole = CompanyRole.Member,
            userId = regularUserId.toString(),
            companyId = documentCollectorId.toString(),
            email = "test@example.com",
            firstName = "Jane",
            lastName = "Doe",
        )

    @Test
    fun `check that admins can patch documents of a data sourcing even if they have no company roles`() {
        setMockSecurityContext(dummyAdminAuthentication)

        performPatchDocumentsRequestAndExpect(status().isOk())
    }

    @Test
    fun `check that regular users cannot patch documents of a data sourcing without proper company roles`() {
        setMockSecurityContext(dummyUserAuthentication)

        doReturn(
            emptyList<CompanyRoleAssignmentExtended>(),
        ).whenever(mockCompanyRolesControllerApi).getExtendedCompanyRoleAssignments(
            userId = regularUserId,
            companyId = documentCollectorId,
        )

        performPatchDocumentsRequestAndExpect(status().isForbidden())
    }

    @Test
    fun `check that users belonging to the document collector company can patch documents of a data sourcing`() {
        setMockSecurityContext(dummyUserAuthentication)

        doReturn(
            listOf(companyRoleAssignmentForRegularUserInDocumentCollector),
        ).whenever(mockCompanyRolesControllerApi).getExtendedCompanyRoleAssignments(
            userId = regularUserId,
            companyId = documentCollectorId,
        )

        performPatchDocumentsRequestAndExpect(status().isOk())
    }
}
