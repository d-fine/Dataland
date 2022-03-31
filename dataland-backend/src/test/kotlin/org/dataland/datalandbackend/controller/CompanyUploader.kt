package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.PostCompanyRequestBody
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CompanyUploader {

    internal fun uploadCompany(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        postCompanyRequestBody: PostCompanyRequestBody
    ) {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/companies")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(postCompanyRequestBody))
        )
            .andExpectAll(
                MockMvcResultMatchers.status().isOk,
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
            )
    }
}
