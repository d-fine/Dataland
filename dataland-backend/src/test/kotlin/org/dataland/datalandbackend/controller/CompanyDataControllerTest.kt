package org.dataland.datalandbackend.controller

import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyBaseManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
internal class CompanyDataControllerTest(
    @Autowired val companyAlterationManager: CompanyAlterationManager,
    @Autowired val companyQueryManager: CompanyQueryManager,
    @Autowired val companyIdentifierRepositoryInterface: CompanyIdentifierRepository,
    @Autowired val companyBaseManager: CompanyBaseManager,
) {
    private final val testLei = "testLei"
    val companyWithTestLei =
        CompanyInformation(
            companyName = "Test Company",
            companyAlternativeNames = null,
            companyContactDetails = null,
            companyLegalForm = null,
            countryCode = "DE",
            headquarters = "Berlin",
            headquartersPostalCode = "8",
            sector = null,
            sectorCodeWz = null,
            website = null,
            isTeaserCompany = null,
            identifiers =
            mapOf(
                IdentifierType.Lei to listOf(testLei),
            ),
            parentCompanyLei = null,
        )
    val companyController =
        CompanyDataController(
            companyAlterationManager,
            companyQueryManager,
            companyIdentifierRepositoryInterface,
            companyBaseManager,
        )

    fun postCompany(): String =
        companyController
            .postCompany(
                companyWithTestLei,
            ).body!!
            .companyId

    @Test
    fun `check that the company id by identifier endpoint works as expected`() {
        mockSecurityContext()

        val expectedCompanyId = postCompany()
        Assertions.assertEquals(
            expectedCompanyId,
            companyController.getCompanyIdByIdentifier(IdentifierType.Lei, testLei).body!!.companyId,
        )
        assertThrows<ResourceNotFoundApiException> {
            companyController.getCompanyIdByIdentifier(IdentifierType.Lei, "nonExistingLei")
        }
    }

    private fun mockSecurityContext() {
        val mockAuthentication =
            AuthenticationMock.mockJwtAuthentication(
                "mocked_uploader",
                "dummy-id",
                setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_UPLOADER),
            )
        val mockSecurityContext = Mockito.mock(SecurityContext::class.java)
        `when`(mockSecurityContext.authentication).thenReturn(mockAuthentication)
        SecurityContextHolder.setContext(mockSecurityContext)
    }

    @Test
    fun `check that the is company valid head endpoint endpoint works as expected`() {
        mockSecurityContext()

        val expectedCompanyId = postCompany()
        assertDoesNotThrow {
            companyController.isCompanyIdValid(expectedCompanyId)
        }

        assertThrows<ResourceNotFoundApiException> {
            companyController.isCompanyIdValid("nonExistingLei")
        }
    }
}
