package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.utils.CompanyUploader
import org.dataland.datalandbackend.utils.TestDataProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles(profiles = ["unprotected"])
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper,
) {

    val testDataProvider = TestDataProvider(objectMapper)
    val testCompanyInformation = testDataProvider.getCompanyInformationWithoutIdentifiers(1).first()

    @Test
    fun `company can be posted`() {
        CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation)
    }

    @Test
    fun `meta info about a specific company can be retrieved by its company Id`() {
        val storedCompany = CompanyUploader().uploadCompany(mockMvc, objectMapper, testCompanyInformation)
        mockMvc.perform(
            get("/companies/${storedCompany.companyId}")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpectAll(
                status().isOk,
                content().contentType(MediaType.APPLICATION_JSON),
            )
    }
}
