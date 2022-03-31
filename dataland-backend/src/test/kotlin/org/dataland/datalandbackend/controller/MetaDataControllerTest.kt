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
internal class MetaDataControllerTest(
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
    fun `list of meta info about data for specific company can be retrieved`() {
        CompanyUploader().uploadCompany(mockMvc, objectMapper, companyInformation)

        mockMvc.perform(
            get("/metadata?companyId=1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().string("[]")
            )
    }
}
