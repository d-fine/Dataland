package org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller.QaReportsMetadataController
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.QaReportMetadataService
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate

@WebMvcTest
@ContextConfiguration(classes = [QaReportsMetadataController::class])
class QaReportsMetadataControllerTest {

    @MockBean
    private lateinit var qaReportMetadataService: QaReportMetadataService

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun whenValidLocalDates_thenNoBadRequest() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/data/reports/metadata")
                .queryParam("minUploadDate", "2022-01-01")
                .queryParam("maxUploadDate", "2022-12-31")
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(MockMvcResultMatchers.status().isOk())

        Mockito.verify(qaReportMetadataService).searchDataAndQaReportMetadata(
            null, true, null,
            LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31), null,
        )
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `check that an invalid date results in a bad request`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/data/reports/metadata")
                .queryParam("minUploadDate", "invalid-date")
                .queryParam("maxUploadDate", "invalid-date")
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)

        Mockito.verify(qaReportMetadataService, Mockito.never()).searchDataAndQaReportMetadata(
            any(), any(), any(),
            any(), any(), any(),
        )
    }
}
