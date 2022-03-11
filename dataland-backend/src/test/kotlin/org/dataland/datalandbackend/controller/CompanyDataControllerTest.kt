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

    fun postCompany(mockMvc: MockMvc, companyName: String) {
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
        postCompany(mockMvc, testCompanyNamesToStore[0])
    }
/*
    @Test
    fun `company can be retrieved by name`() {
        postResponse = postCompany(mockMvc, testCompanyNamesToStore[0])

        mockMvc.perform(
            get("/company/{"$testCompanyNamesToStore[0]"})
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("\$.companyName").value(testCompanyNamesToStore[0]))
            .andExpect(jsonPath("\$.companyId").value(postReponse.companyId))
    }
    */
/*
    TEMPLATE:
    @Test
    fun `list the data`() {
        for (dataset in storedDataSets)
            uploadDataSet(mockMvc, dataset)
        mockMvc.perform(
            get("/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(status().isOk, content().contentType(MediaType.APPLICATION_JSON))
    }*/
}
