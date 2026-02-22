package org.dataland.datasourcingservice.serviceTests

import org.dataland.datasourcingservice.utils.COMPANY_ID_1
import org.dataland.datasourcingservice.utils.DATA_TYPE_1
import org.dataland.datasourcingservice.utils.REPORTING_PERIOD_1
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

private const val PRIORITY_PATH = "$.priority"

class DataSourcingPriorityControllerTest : AbstractDataSourcingControllerTest() {
    @Test
    fun `admins can patch priority`() {
        setMockSecurityContext(dummyAdminAuthentication)
        mockMvc
            .perform(
                patch("/data-sourcing/$dataSourcingId/priority")
                    .queryParam("priority", "3")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath(PRIORITY_PATH).value(3))
    }

    @Test
    fun `regular users cannot patch priority`() {
        setMockSecurityContext(dummyUserAuthentication)
        mockMvc
            .perform(
                patch("/data-sourcing/$dataSourcingId/priority")
                    .queryParam("priority", "3")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden())
    }

    @Test
    fun `admin can see priority in get data sourcing by id`() {
        setMockSecurityContext(dummyAdminAuthentication)
        mockMvc
            .perform(
                get("/data-sourcing/$dataSourcingId")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath(PRIORITY_PATH).value(10))
    }

    @Test
    fun `regular user without provider role cannot see priority in get data sourcing by id`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, emptyList())
        stubRoleAssignments(regularUserId, dataExtractorId, emptyList())
        mockMvc
            .perform(
                get("/data-sourcing/$dataSourcingId")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath(PRIORITY_PATH).value(null as Any?))
    }

    @Test
    fun `data provider can see priority in get data sourcing by id`() {
        setMockSecurityContext(dummyUserAuthentication)
        stubRoleAssignments(regularUserId, documentCollectorId, listOf(memberAssignmentForDocumentCollector))
        mockMvc
            .perform(
                get("/data-sourcing/$dataSourcingId")
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath(PRIORITY_PATH).value(10))
    }

    @Test
    fun `admin can get priorities for a list of data dimensions`() {
        setMockSecurityContext(dummyAdminAuthentication)
        val body =
            """[{"companyId": "$COMPANY_ID_1", "dataType": "$DATA_TYPE_1", "reportingPeriod": "$REPORTING_PERIOD_1"}]"""
        mockMvc
            .perform(
                post("/data-sourcing/priorities")
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].priority").value(10))
    }

    @Test
    fun `regular user cannot get priorities for data dimensions`() {
        setMockSecurityContext(dummyUserAuthentication)
        val body =
            """[{"companyId": "$COMPANY_ID_1", "dataType": "$DATA_TYPE_1", "reportingPeriod": "$REPORTING_PERIOD_1"}]"""
        mockMvc
            .perform(
                post("/data-sourcing/priorities")
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(securityContext(mockSecurityContext)),
            ).andExpect(status().isForbidden())
    }
}
