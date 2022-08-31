package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.utils.CompanyUploader
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(profiles = ["unprotected"])
internal class MetaDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper
) {
    val testDataProvider = TestDataProvider(objectMapper)

    @Test
    fun `list of meta info about data for specific company can be retrieved`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).last()
        val storedCompany = CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation)
        mockMvc.perform(
            get("/metadata?companyId=${storedCompany.companyId}")
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
