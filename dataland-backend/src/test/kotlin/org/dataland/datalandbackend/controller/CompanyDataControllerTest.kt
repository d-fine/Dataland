package org.dataland.datalandbackend.controller

import jakarta.transaction.Transactional
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.services.CompanyAlterationManager
import org.dataland.datalandbackend.services.CompanyBaseManager
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.datapoints.AssembledDataManager
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.exceptions.SEARCHSTRING_TOO_SHORT_THRESHOLD
import org.dataland.datalandbackendutils.exceptions.SEARCHSTRING_TOO_SHORT_VALIDATION_MESSAGE
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
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
import kotlin.reflect.jvm.javaMethod

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
internal class CompanyDataControllerTest(
    @Autowired val companyAlterationManager: CompanyAlterationManager,
    @Autowired val companyQueryManager: CompanyQueryManager,
    @Autowired val companyIdentifierRepositoryInterface: CompanyIdentifierRepository,
    @Autowired val companyBaseManager: CompanyBaseManager,
    @Autowired private val assembledDataManager: AssembledDataManager,
) {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    lateinit var companyController: CompanyDataController

    @BeforeEach
    fun initCompanyController() {
        companyController =
            CompanyDataController(
                companyAlterationManager,
                companyQueryManager,
                companyIdentifierRepositoryInterface,
                companyBaseManager,
                assembledDataManager,
            )
    }

    private final val testLei = "testLei"
    private final val testChildLei = "testChildLei"
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
    val companyWithParent =
        companyWithTestLei.copy(
            companyName = "Test Company Child",
            identifiers = mapOf(IdentifierType.Lei to listOf(testChildLei)),
            parentCompanyLei = testLei,
        )

    fun postCompany(company: CompanyInformation = companyWithTestLei): String =
        companyController
            .postCompany(
                company,
            ).body!!
            .companyId

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
    fun `getCompanies should fail validation when searchString is too short`() {
        val method = CompanyDataController::getCompaniesBySearchString.javaMethod!!
        val parameters = arrayOf("aa", 100)

        val violations =
            validator.forExecutables().validateParameters(
                companyController,
                method,
                parameters,
            )

        assertFalse(violations.isEmpty())
        val violation = violations.iterator().next()
        assertEquals("$SEARCHSTRING_TOO_SHORT_VALIDATION_MESSAGE: $SEARCHSTRING_TOO_SHORT_THRESHOLD", violation.message)
    }

    @Test
    fun `getCompanies should pass validation when searchString is long enough`() {
        val method = CompanyDataController::getCompaniesBySearchString.javaMethod!!
        val parameters = arrayOf("aaa", 100)

        val violations =
            validator.forExecutables().validateParameters(
                companyController,
                method,
                parameters,
            )

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `getCompanies should pass validation when searchString null or empty`() {
        val method = CompanyDataController::getCompaniesBySearchString.javaMethod!!
        val parametersList = arrayOf(arrayOf("", 100), arrayOf(null, 100))

        for (parameters in parametersList) {
            val violations =
                validator.forExecutables().validateParameters(
                    companyController,
                    method,
                    parameters,
                )

            assertTrue(violations.isEmpty())
        }
    }

    @Test
    fun `getCompanySubsidiariesByParentId fails when parentId is not found`() {
        mockSecurityContext()

        postCompany(companyWithTestLei)

        assertThrows<ResourceNotFoundApiException> {
            companyController.getCompanySubsidiariesByParentId("invalid company id")
        }
    }

    @Test
    fun `getCompanySubsidiariesByParentId returns child as expected`() {
        mockSecurityContext()

        val parentCompanyId = postCompany(companyWithTestLei)
        val childCompanyId = postCompany(companyWithParent)

        assertIterableEquals(
            companyController.getCompanySubsidiariesByParentId(childCompanyId).body,
            listOf<BasicCompanyInformation>(),
        )
        val basicChildCompanyInformationList = companyController.getCompanySubsidiariesByParentId(parentCompanyId).body
        assertEquals(basicChildCompanyInformationList?.size, 1)
        assertEquals(basicChildCompanyInformationList?.get(0)?.companyId, childCompanyId)
        assertEquals(basicChildCompanyInformationList?.get(0)?.companyName, companyWithParent.companyName)
        assertEquals(basicChildCompanyInformationList?.get(0)?.lei, testChildLei)
    }
}
