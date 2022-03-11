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

    val testCompanyNamesToStore = listOf("Imaginary-Company_I", "Fantasy-Company_II", "Dream-Company_III")

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
        uploadCompany(mockMvc, testCompanyNamesToStore[0])
    }

    @Test
    fun `company can be retrieved by name`() {
        uploadCompany(mockMvc, testCompanyNamesToStore[0])

        mockMvc.perform(
            get("/company/${testCompanyNamesToStore[0]}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
    }

    @Test
    fun `all-company-list can be retrieved and is empty because no company was posted`() {
        mockMvc.perform(
            get("/company")
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

    @Test
    fun `list of all data sets for a specific company Id can be retrieved and is empty because no data was posted`() {
        uploadCompany(mockMvc, testCompanyNamesToStore[0])

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
