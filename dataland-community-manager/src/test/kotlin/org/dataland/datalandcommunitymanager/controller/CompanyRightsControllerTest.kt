package org.dataland.datalandcommunitymanager.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandcommunitymanager.DatalandCommunityManager
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRight
import org.dataland.datalandcommunitymanager.model.companyRights.CompanyRightAssignment
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID

@SpringBootTest(classes = [DatalandCommunityManager::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
class CompanyRightsControllerTest
    @Autowired
    constructor(
        val mockMvc: MockMvc,
    ) {
        @MockitoBean
        private lateinit var companyDataControllerClient: CompanyDataControllerApi

        /** Build the JSON string for a company right assignment.
         *
         * @param companyId The ID of the company.
         * @param companyRight The company right to assign.
         * @return The JSON string representing the company right assignment.
         */
        private fun buildCompanyRightAssignmentJson(
            companyId: String,
            companyRight: CompanyRight,
        ): String = defaultObjectMapper.writeValueAsString(CompanyRightAssignment(companyId, companyRight))

        /** Post a company right assignment and verify the response.
         *
         * @param companyId The ID of the company.
         * @param companyRight The company right to assign.
         * @param expectedStatus The expected HTTP status of the response.
         */
        private fun postCompanyRightAssignment(
            companyId: String,
            companyRight: CompanyRight,
            expectedStatus: ResultMatcher = MockMvcResultMatchers.status().isOk,
        ) {
            val rightsAssignment = buildCompanyRightAssignmentJson(companyId, companyRight)

            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/company-rights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightsAssignment),
                ).andExpect(expectedStatus)
                .let { postResponse ->
                    if (expectedStatus == MockMvcResultMatchers.status().isOk) {
                        postResponse
                            .andExpect(MockMvcResultMatchers.jsonPath(".companyId").value(companyId))
                            .andExpect(MockMvcResultMatchers.jsonPath(".companyRight").value(companyRight.toString()))
                    }
                }
        }

        /** Get and verify the company rights for a given company ID.
         *
         * @param companyId The ID of the company.
         * @param expectedRights The expected list of company rights.
         */
        private fun getAndVerifyCompanyRights(
            companyId: String,
            expectedRights: List<CompanyRight>,
        ) {
            val expectedRightsJsonArray = defaultObjectMapper.writeValueAsString(expectedRights)

            mockMvc
                .perform(
                    MockMvcRequestBuilders.get("/company-rights/$companyId"),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.content().json(expectedRightsJsonArray))
        }

        /** Delete a company right assignment and verify the response.
         *
         * @param companyId The ID of the company.
         * @param companyRight The company right to delete.
         * @param expectedStatus The expected HTTP status of the response.
         */
        private fun deleteCompanyRightAssignment(
            companyId: String,
            companyRight: CompanyRight,
            expectedStatus: ResultMatcher = MockMvcResultMatchers.status().isOk,
        ) = mockMvc
            .perform(
                MockMvcRequestBuilders
                    .delete("/company-rights")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(buildCompanyRightAssignmentJson(companyId, companyRight)),
            ).andExpect(expectedStatus)

        @Test
        @WithMockUser(username = "data_admin", roles = ["ADMIN"])
        fun `verify that company rights can be assigned and retrieved`() {
            val companyId = UUID.randomUUID().toString()

            postCompanyRightAssignment(companyId, CompanyRight.Member)
            getAndVerifyCompanyRights(companyId, listOf(CompanyRight.Member))

            postCompanyRightAssignment(companyId, CompanyRight.Provider)
            getAndVerifyCompanyRights(companyId, listOf(CompanyRight.Member, CompanyRight.Provider))
        }

        @Test
        @WithMockUser(username = "data_admin", roles = ["ADMIN"])
        fun `verify that duplicate company right assignments are idempotent`() {
            val companyId = UUID.randomUUID().toString()

            postCompanyRightAssignment(companyId, CompanyRight.Member)
            getAndVerifyCompanyRights(companyId, listOf(CompanyRight.Member))

            postCompanyRightAssignment(companyId, CompanyRight.Member)
            getAndVerifyCompanyRights(companyId, listOf(CompanyRight.Member))
        }

        @Test
        @WithMockUser(username = "data_admin", roles = ["ADMIN"])
        fun `verify that multiple companies have isolated rights`() {
            val companyId1 = UUID.randomUUID().toString()
            val companyId2 = UUID.randomUUID().toString()

            postCompanyRightAssignment(companyId1, CompanyRight.Member)
            getAndVerifyCompanyRights(companyId1, listOf(CompanyRight.Member))
            getAndVerifyCompanyRights(companyId2, emptyList())

            postCompanyRightAssignment(companyId2, CompanyRight.Provider)
            getAndVerifyCompanyRights(companyId1, listOf(CompanyRight.Member))
            getAndVerifyCompanyRights(companyId2, listOf(CompanyRight.Provider))
        }

        @Test
        @WithMockUser(username = "data_admin", roles = ["ADMIN"])
        fun `verify that no rights are returned for companies without assignments`() =
            getAndVerifyCompanyRights(UUID.randomUUID().toString(), emptyList())

        @Test
        @WithMockUser(username = "data_admin", roles = ["ADMIN"])
        fun `verify that non-existent rights cannot be deleted`() =
            deleteCompanyRightAssignment(
                UUID.randomUUID().toString(),
                CompanyRight.Provider,
                MockMvcResultMatchers.status().isNotFound,
            )

        @Test
        @WithMockUser(username = "data_uploader", roles = ["UPLOADER"])
        fun `verify that non-admin users cannot manage company rights`() {
            val companyId = UUID.randomUUID().toString()
            postCompanyRightAssignment(companyId, CompanyRight.Provider, MockMvcResultMatchers.status().isForbidden)
            deleteCompanyRightAssignment(companyId, CompanyRight.Provider, MockMvcResultMatchers.status().isForbidden)
        }

        @Test
        @WithMockUser(username = "data_admin", roles = ["ADMIN"])
        fun `verify that company right assignments can be deleted`() {
            val companyId = UUID.randomUUID().toString()
            postCompanyRightAssignment(companyId, CompanyRight.Provider)
            deleteCompanyRightAssignment(companyId, CompanyRight.Provider)
            getAndVerifyCompanyRights(companyId, emptyList())
        }

        @Test
        @WithMockUser(username = "data_admin", roles = ["ADMIN"])
        fun `verify that posting right assignments for non-existing companies fails`() {
            val companyId = UUID.randomUUID().toString()
            doThrow(ClientException()).whenever(companyDataControllerClient).isCompanyIdValid(companyId)
            postCompanyRightAssignment(companyId, CompanyRight.Provider, MockMvcResultMatchers.status().isNotFound)
        }
    }
