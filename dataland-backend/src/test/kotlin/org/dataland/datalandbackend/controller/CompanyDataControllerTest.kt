package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.TestDataProvider
import org.dataland.datalandbackend.model.StoredCompany
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper
) {

    val dataProvider = TestDataProvider(objectMapper)
    val testCompanyInformation = dataProvider.getCompanyInformation(1).last()

    @Test
    fun `company can be posted`() {
        CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation)
    }

    @Test
    fun `meta info about a specific company can be retrieved by its company Id`() {
        val response = CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation)
        val result: StoredCompany = objectMapper.readValue(
            response,
            object : TypeReference<StoredCompany>() {}
        )
        mockMvc.perform(
            get("/companies/${result.companyId}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON)
            )
    }
}
