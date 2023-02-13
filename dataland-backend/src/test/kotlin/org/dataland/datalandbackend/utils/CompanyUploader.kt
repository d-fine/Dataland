package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.CompanyInformation
import org.dataland.datalandbackend.model.StoredCompany
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CompanyUploader {

    internal fun uploadCompany(
        mockMvc: MockMvc,
        objectMapper: ObjectMapper,
        companyInformation: CompanyInformation,
    ): StoredCompany {
        val request = mockMvc.perform(
            MockMvcRequestBuilders.post("/public/companies")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(companyInformation)),
        )
            .andExpectAll(
                MockMvcResultMatchers.status().isOk,
                MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            ).andReturn()
        return objectMapper.readValue(
            request.response.contentAsString,
            object : TypeReference<StoredCompany>() {},
        )
    }
}
