package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.Date

@SpringBootTest
@AutoConfigureMockMvc
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper
) {

    val companyInformation = CompanyInformation(
        companyName = "Test-Company_I",
        headquarters = "Test-Headquarters_I",
        industrialSector = "Test-IndustrialSector_I",
        marketCap = BigDecimal(100),
        reportingDateOfMarketCap = Date()
    )

    @Test
    fun `company can be posted`() {
        CompanyUploader().uploadCompany(mockMvc, objectMapper, companyInformation)
    }

    @Test
    fun `company can be retrieved by name`() {
        CompanyUploader().uploadCompany(mockMvc, objectMapper, companyInformation)

        mockMvc.perform(
            get("/companies?companyName=${companyInformation.companyName}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `meta info about a specific company can be retrieved by its company Id`() {
        CompanyUploader().uploadCompany(mockMvc, objectMapper, companyInformation)

        mockMvc.perform(
            get("/companies/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON)
            )
    }
}
