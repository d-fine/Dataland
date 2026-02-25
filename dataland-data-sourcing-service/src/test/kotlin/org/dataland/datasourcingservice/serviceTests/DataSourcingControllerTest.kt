package org.dataland.datasourcingservice.serviceTests

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.utils.BaseDataSourcingControllerTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class DataSourcingControllerTest : BaseDataSourcingControllerTest() {
    private val providerCompanyId: UUID = UUID.randomUUID()
    private val documentId = "my-document-hash"
    private val dateOfNextSourcingAttempt = "2026-01-01"

    private fun setupCompanyExistsValidatorMocks() {
        val mockCall = mock<okhttp3.Call>()
        val dummyBody =
            object : ResponseBody() {
                override fun contentType(): okhttp3.MediaType? = null

                override fun contentLength(): Long = 0

                override fun source(): BufferedSource = Buffer()
            }
        val response =
            Response
                .Builder()
                .request(
                    Request
                        .Builder()
                        .url("http://localhost/dummy")
                        .build(),
                ).protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(dummyBody)
                .build()

        whenever(authenticatedOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(response)
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

    private fun performGetDataSourcingByCompanyId(
        resultMatcher: ResultMatcher,
        companyId: UUID = providerCompanyId,
    ) {
        mockMvc
            .perform(
                get("/data-sourcing/provider/$companyId")
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
        setupCompanyExistsValidatorMocks()
        setMockSecurityContext(dummyAdminAuthentication)
        performGetDataSourcingByCompanyId(status().isOk())
    }

    @Test
    fun `regular users can get data sourcings by company ID`() {
        setupCompanyExistsValidatorMocks()
        setMockSecurityContext(dummyUserAuthentication)
        performGetDataSourcingByCompanyId(status().isOk())
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
        doNothing()
            .whenever(
                mockCloudEventMessageHandler,
            ).buildCEMessageAndSendToQueue(
                any(),
                any(),
                any(),
                any(),
                any(),
            )
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
