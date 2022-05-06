package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CompanyUploader {

    internal fun uploadCompany(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        companyInformation: CompanyInformation
    ): String {
        val request = mockMvc.perform(
            MockMvcRequestBuilders.post("/companies")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(companyInformation))
        )
            .andExpectAll(
                MockMvcResultMatchers.status().isOk,
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
            ).andReturn()
        return request.response.contentAsString
    }
}
