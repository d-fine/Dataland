package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.service.IncompleteCompanyInformationService
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID

class SubscribedCompaniesControllerTest : AbstractPortfolioControllerTest() {
    @MockitoBean
    private lateinit var incompleteCompanyInformationService: IncompleteCompanyInformationService

    private val reviewerAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "REVIEWER",
            userId = UUID.randomUUID().toString(),
            roles = setOf(DatalandRealmRole.ROLE_REVIEWER, DatalandRealmRole.ROLE_USER),
        )
    private val uploaderAuthentication =
        AuthenticationMock.mockJwtAuthentication(
            username = "UPLOADER",
            userId = UUID.randomUUID().toString(),
            roles = setOf(DatalandRealmRole.ROLE_UPLOADER, DatalandRealmRole.ROLE_USER),
        )

    @BeforeEach
    fun setupSubscribedCompaniesTest() {
        reset(incompleteCompanyInformationService)
    }

    private fun performGet(statusMatcher: ResultMatcher) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/subscribed-companies/incomplete-information")
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    private fun performPost(statusMatcher: ResultMatcher) {
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .post("/subscribed-companies/incomplete-information/c1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(statusMatcher)
    }

    private fun authenticationFor(role: String) = if (role == "ADMIN") dummyAdminAuthentication else reviewerAuthentication

    @ParameterizedTest
    @ValueSource(strings = ["REVIEWER", "ADMIN"])
    fun `privileged roles can read the log`(role: String) {
        setMockSecurityContext(authenticationFor(role))
        whenever(incompleteCompanyInformationService.getIncompleteCompanyIds()).thenReturn(setOf("c1"))
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/subscribed-companies/incomplete-information")
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value("c1"))
    }

    @Test
    fun `plain user cannot read the log`() {
        setMockSecurityContext(dummyUserAuthentication)
        performGet(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `old endpoint no longer exists`() {
        setMockSecurityContext(reviewerAuthentication)
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get("/subscribed-companies/incomplete-fye-information")
                    .with(SecurityMockMvcRequestPostProcessors.securityContext(mockSecurityContext)),
            ).andExpect(MockMvcResultMatchers.status().is4xxClientError)
    }

    @Test
    fun `admin can add a company to the log`() {
        setMockSecurityContext(dummyAdminAuthentication)
        performPost(MockMvcResultMatchers.status().isOk)
        verify(incompleteCompanyInformationService).registerIncompleteCompany(eq("c1"), any())
    }

    @Test
    fun `uploader cannot add a company to the log`() {
        setMockSecurityContext(uploaderAuthentication)
        performPost(MockMvcResultMatchers.status().isForbidden)
    }

    @Test
    fun `unknown company results in not found`() {
        setMockSecurityContext(dummyAdminAuthentication)
        whenever(incompleteCompanyInformationService.registerIncompleteCompany(any(), any()))
            .thenAnswer { throw ResourceNotFoundApiException("not found", "not found") }
        performPost(MockMvcResultMatchers.status().isNotFound)
    }
}
