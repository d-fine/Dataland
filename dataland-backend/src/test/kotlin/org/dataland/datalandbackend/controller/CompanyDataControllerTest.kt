package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.utils.CompanyUploader
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles(profiles = ["unprotected"])
internal class CompanyDataControllerTest(
    @Autowired var mockMvc: MockMvc,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired val companyAlterationManager: CompanyAlterationManager,
    @Autowired val companyQueryManager: CompanyQueryManager,
    @Autowired val companyIdentifierRepositoryInterface: CompanyIdentifierRepository,
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

    val testLei = "testLei"
    val companyWithTestLei = CompanyInformation(
        companyName = "Test Company",
        companyAlternativeNames = null,
        companyLegalForm = null,
        countryCode = "DE",
        headquarters = "Berlin",
        headquartersPostalCode = "8",
        sector = null,
        website = null,
        isTeaserCompany = null,
        identifiers = mapOf(
            IdentifierType.Lei to listOf(testLei),
        ),
    )

    @Test
    fun `check that the company id by identifier endpoint works as expected`() {
        mockSecurityContext()
        val companyController = CompanyDataController(
            companyAlterationManager,
            companyQueryManager,
            companyIdentifierRepositoryInterface,
        )
        val expectedCompanyId = companyController.postCompany(
            companyWithTestLei,
        ).body!!.companyId
        Assertions.assertEquals(
            expectedCompanyId,
            companyController.getCompanyIdByIdentifier(IdentifierType.Lei, testLei).body!!.companyId,
        )
        assertThrows<ResourceNotFoundApiException> {
            companyController.getCompanyIdByIdentifier(IdentifierType.Lei, "nonExistingLei")
        }
    }

    private fun mockSecurityContext() {
        val mockAuthentication = AuthenticationMock.mockJwtAuthentication(
            "mocked_uploader",
            "dummy-id",
            setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER),
        )
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
    }
}
