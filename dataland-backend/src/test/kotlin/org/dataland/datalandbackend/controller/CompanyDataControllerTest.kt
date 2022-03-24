package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.PostCompanyRequestBody
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

@SpringBootTest
@AutoConfigureMockMvc
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper
) {

    val testCompanyName = "Imaginary-Company_I"

    fun uploadCompany(mockMvc: MockMvc, companyName: String) {
        val postCompanyRequestBody = PostCompanyRequestBody(companyName = companyName)
        mockMvc.perform(
            post("/companies")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(postCompanyRequestBody))
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
            get("/companies?companyName=$testCompanyName")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `list of all data sets for a specific company Id can be retrieved and is empty because no data was posted`() {
        uploadCompany(mockMvc, testCompanyName)

        mockMvc.perform(
            get("/companies/1/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().string("[]")
            )
    }

    @Test
    fun `meta info about a specific company can be retrieved by its company Id`() {
        uploadCompany(mockMvc, testCompanyName)

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
