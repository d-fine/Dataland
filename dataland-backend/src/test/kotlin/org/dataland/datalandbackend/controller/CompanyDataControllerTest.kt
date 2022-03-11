package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// TODO Completely new unit tests needed here for ne CompanyAPI

@SpringBootTest
@AutoConfigureMockMvc
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper
) {

    val testCompanyName = "Imaginary-Company_I"

    fun uploadCompany(mockMvc: MockMvc, companyName: String) {
        mockMvc.perform(
            post("/company")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(companyName))
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `company can be posted`() {
        uploadCompany(mockMvc, testCompanyName)
    }

    @Test
    fun `company can be retrieved by name`() {
        uploadCompany(mockMvc, testCompanyName)

        mockMvc.perform(
            get("/company/${testCompanyName}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))


    }

    @Test
    fun `all-company-list can be retrieved`() {
        mockMvc.perform(
            get("/company")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
            )
    }

    @Test
    fun `list of all data sets for a specific company Id can be retrieved and is empty because no data was posted`() {
        uploadCompany(mockMvc, testCompanyName)

        mockMvc.perform(
            get("/company/1/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                //jsonPath("$s").value("")
                content().string("[]")
            )

    }

}
