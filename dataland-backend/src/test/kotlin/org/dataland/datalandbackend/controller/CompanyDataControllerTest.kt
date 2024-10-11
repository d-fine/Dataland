package org.dataland.datalandbackend.controller

import jakarta.transaction.Transactional
import jakarta.validation.Validation
import jakarta.validation.Validator
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
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
    // Validator for validation tests
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    // Reusing the controller for both validation and old tests
    val companyController =
        CompanyDataController(
            companyAlterationManager,
            companyQueryManager,
            companyIdentifierRepositoryInterface,
            companyBaseManager,
        )

    // Mocking a company for the old tests
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

    // Utility to post a company for the old tests
    fun postCompany(): String =
        companyController
            .postCompany(
                companyWithTestLei,
            ).body!!
            .companyId

    // Old test: Checking company ID by identifier
    @Test
    fun `check that the company id by identifier endpoint works as expected`() {
        mockSecurityContext()

        val expectedCompanyId = postCompany()
        assertEquals(
            expectedCompanyId,
            companyController.getCompanyIdByIdentifier(IdentifierType.Lei, testLei).body!!.companyId,
        )
        assertThrows<ResourceNotFoundApiException> {
            companyController.getCompanyIdByIdentifier(IdentifierType.Lei, "nonExistingLei")
        }
    }

    // Old test: Checking if company ID is valid
    @Test
    fun `check that the is company valid head endpoint works as expected`() {
        mockSecurityContext()

        val expectedCompanyId = postCompany()
        assertDoesNotThrow {
            companyController.isCompanyIdValid(expectedCompanyId)
        }

        assertThrows<ResourceNotFoundApiException> {
            companyController.isCompanyIdValid("nonExistingLei")
        }
    }

    // Mocking security context
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

    // New test: Validation test for too short searchString
    @Test
    fun `getCompanies should fail validation when searchString is too short`() {
        val method =
            CompanyDataController::class.java.getMethod(
                "getCompanies",
                String::class.java,
                Set::class.java,
                Set::class.java,
                Set::class.java,
                Int::class.javaObjectType,
                Int::class.javaObjectType,
            )
        val parameters = arrayOf("a", null, null, null, null, null)

        val violations =
            validator.forExecutables().validateParameters(
                companyController,
                method,
                parameters,
            )

        assertFalse(violations.isEmpty())
        val violation = violations.iterator().next()
        assertEquals("Search string must be at least 2 non-nullish characters long.", violation.message)
    }

    // New test: Validation test for valid searchString
    @Test
    fun `getCompanies should pass validation when searchString is valid`() {
        val method =
            CompanyDataController::class.java.getMethod(
                "getCompanies",
                String::class.java,
                Set::class.java,
                Set::class.java,
                Set::class.java,
                Int::class.javaObjectType,
                Int::class.javaObjectType,
            )
        val parameters = arrayOf("ValidCompanyName", null, null, null, null, null)

        val violations =
            validator.forExecutables().validateParameters(
                companyController,
                method,
                parameters,
            )

        assertTrue(violations.isEmpty())
    }
}
