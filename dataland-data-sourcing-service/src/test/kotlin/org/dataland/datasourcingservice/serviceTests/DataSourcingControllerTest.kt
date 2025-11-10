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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(classes = [DatalandDataSourcingService::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureMockMvc
class DataSourcingControllerTest(
    @Autowired private val dataSourcingRepository: DataSourcingRepository,
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var mockCompanyRolesControllerApi: CompanyRolesControllerApi

    @MockitoBean
    private lateinit var mockDataSourcingValidator: DataSourcingValidator

    private val dataBaseCreationUtils = DataBaseCreationUtils(dataSourcingRepository = dataSourcingRepository)

    private val mockSecurityContext = mock<SecurityContext>()

    private val adminUserId = UUID.randomUUID()
    private val regularUserId = UUID.randomUUID()

    private val dummyAdminAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_ADMIN",
            userId = adminUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_ADMIN),
        )

    private val dummyUserAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "DATA_USER",
            userId = regularUserId.toString(),
            roles = setOf(DatalandRealmRole.ROLE_USER),
        )

    private val dataSourcingId = UUID.randomUUID()
    private val documentCollectorId = UUID.randomUUID()
    private val dataExtractorId = UUID.randomUUID()
    private val providerCompanyId = UUID.randomUUID()
    private val documentId = "my-document-hash"
    private val dateOfNextSourcingAttempt = "2026-01-01"

    private val generalMemberAssignment =
        CompanyRoleAssignmentExtended(
            companyRole = CompanyRole.Member,
            userId = regularUserId.toString(),
            companyId = providerCompanyId.toString(),
            email = "test@example.com",
            firstName = "Jane",
            lastName = "Doe",
        )

    private val memberAssignmentForDocumentCollector =
        generalMemberAssignment.copy(companyId = documentCollectorId.toString())

    private val memberAssignmentForDataExtractor =
        generalMemberAssignment.copy(companyId = dataExtractorId.toString())

    @BeforeEach
    fun setup() {
        reset(
            mockSecurityContext,
            mockCompanyRolesControllerApi,
            mockDataSourcingValidator,
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

    private fun setMockSecurityContext(authentication: DatalandJwtAuthentication) {
        doReturn(authentication).whenever(mockSecurityContext).authentication
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    private fun stubRoleAssignments(
        userId: UUID,
        companyId: UUID,
        roles: List<CompanyRoleAssignmentExtended>,
    ) {
        doReturn(roles)
            .whenever(mockCompanyRolesControllerApi)
            .getExtendedCompanyRoleAssignments(userId = userId, companyId = companyId)
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

    private fun performPatchDateOfNextDocumentSourcingAttempt(resultMatcher: ResultMatcher) {
        mockMvc
            .perform(
                patch("/data-sourcing/$dataSourcingId/document-sourcing-attempt")
                    .queryParam("dateOfNextDocumentSourcingAttempt", dateOfNextSourcingAttempt)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(resultMatcher)
    }

    private fun performGetDataSourcingByCompanyId(resultMatcher: ResultMatcher) {
        mockMvc
            .perform(
                get("/data-sourcing/provider/$providerCompanyId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(resultMatcher)
    }

    private fun performPatchStateAndExpect(
        state: DataSourcingState,
        resultMatcher: ResultMatcher,
    ) {
        mockMvc
            .perform(
                patch("/data-sourcing/$dataSourcingId/state")
                    .queryParam("state", state.name)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(resultMatcher)
    }

    @Test
    fun `admins can patch documents without company roles`() {
        setMockSecurityContext(dummyAdminAuthentication)
        performPatchDocumentsRequestAndExpect(status().isOk())
    }

    @Test
    fun `regular users cannot patch documents without company roles`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, emptyList())
        performPatchDocumentsRequestAndExpect(status().isForbidden())
    }

    @Test
    fun `users with company roles can patch documents`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, listOf(memberAssignmentForDocumentCollector))
        performPatchDocumentsRequestAndExpect(status().isOk())
    }

    @Test
    fun `admins can patch next document sourcing attempt date`() {
        setMockSecurityContext(dummyAdminAuthentication)
        performPatchDateOfNextDocumentSourcingAttempt(status().isOk())
    }

    @Test
    fun `regular users cannot patch next attempt date without company roles`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, emptyList())
        performPatchDateOfNextDocumentSourcingAttempt(status().isForbidden())
    }

    @Test
    fun `users with company roles can patch next attempt date`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, listOf(memberAssignmentForDocumentCollector))
        performPatchDateOfNextDocumentSourcingAttempt(status().isOk())
    }

    @Test
    fun `admins can get data sourcings by company ID`() {
        setMockSecurityContext(dummyAdminAuthentication)
        performGetDataSourcingByCompanyId(status().isOk())
    }

    @Test
    fun `regular users cannot get data sourcings by company ID without company roles`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, providerCompanyId, emptyList())
        performGetDataSourcingByCompanyId(status().isForbidden())
    }

    @Test
    fun `users with company roles can get data sourcings for their own company ID`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, providerCompanyId, listOf(generalMemberAssignment))
        performGetDataSourcingByCompanyId(status().isOk())
    }

    @Test
    fun `users with company roles cannot get data sourcings for other company IDs`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, providerCompanyId, emptyList())
        stubRoleAssignments(regularUserId, documentCollectorId, listOf(memberAssignmentForDocumentCollector))
        performGetDataSourcingByCompanyId(status().isForbidden())
    }

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `admins can patch data sourcing state to any state`(state: DataSourcingState) {
        setMockSecurityContext(dummyAdminAuthentication)
        performPatchStateAndExpect(state, status().isOk())
    }

    @ParameterizedTest
    @ValueSource(strings = ["DocumentSourcingDone", "NonSourceable"])
    fun `document collectors can set allowed states`(stateName: String) {
        val state = DataSourcingState.valueOf(stateName)
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, listOf(memberAssignmentForDocumentCollector))
        performPatchStateAndExpect(state, status().isOk())
    }

    @ParameterizedTest
    @ValueSource(strings = ["Initialized", "DocumentSourcing", "DataExtraction", "DataVerification", "Done"])
    fun `document collectors cannot set restricted states`(stateName: String) {
        val state = DataSourcingState.valueOf(stateName)
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, listOf(memberAssignmentForDocumentCollector))
        performPatchStateAndExpect(state, status().isForbidden())
    }

    @ParameterizedTest
    @ValueSource(strings = ["NonSourceable"])
    fun `data extractors can set allowed state NonSourceable`(stateName: String) {
        val state = DataSourcingState.valueOf(stateName)
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, dataExtractorId, listOf(memberAssignmentForDataExtractor))
        performPatchStateAndExpect(state, status().isOk())
    }

    @ParameterizedTest
    @ValueSource(strings = ["Initialized", "DocumentSourcing", "DocumentSourcingDone", "DataExtraction", "DataVerification", "Done"])
    fun `data extractors cannot set restricted states`(stateName: String) {
        val state = DataSourcingState.valueOf(stateName)
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, dataExtractorId, listOf(memberAssignmentForDataExtractor))
        performPatchStateAndExpect(state, status().isForbidden())
    }

    @ParameterizedTest
    @EnumSource(DataSourcingState::class)
    fun `regular users without company roles cannot set any state`(state: DataSourcingState) {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, emptyList())
        stubRoleAssignments(regularUserId, dataExtractorId, emptyList())
        performPatchStateAndExpect(state, status().isForbidden())
    }
}
