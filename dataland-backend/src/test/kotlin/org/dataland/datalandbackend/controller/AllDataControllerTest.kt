package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyAssociatedData
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
internal class AllDataControllerTest(
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
    fun `list of meta info about data for specific company can be retrieved`() {
        uploadCompany(mockMvc, testCompanyName)

        mockMvc.perform(
            get("/data?companyId=1")
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
    fun `list of meta info about data of specific data type can be retrieved`() {
        mockMvc.perform(
            get("/data/1/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().string("[]")
            )
    }

    // TODO Search for data with new all data endpoint after upload
    @Test
    fun `upload data for a company and retrieve meta info about that data by searching for the data Id`() {
        mockMvc.perform(
            post("/eutaxonomies")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsBytes(CompanyAssociatedData(companyId = "1", data = "dummy"))
                )
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().string("1")
            )

        mockMvc.perform(
            get("/data/1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsBytes(CompanyAssociatedData(companyId = "1", data = "dummy"))
                )
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
                content().string("1")
            )
    }
}
