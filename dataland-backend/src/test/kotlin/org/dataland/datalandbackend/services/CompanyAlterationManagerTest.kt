package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.exceptions.DuplicateIdentifierApiException
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [DatalandBackend::class])
@Testcontainers
@Transactional
@Rollback
class CompanyAlterationManagerTest {
    @Autowired
    private lateinit var companyAlterationManager: CompanyAlterationManager

    @Autowired
    private lateinit var companyIdentifierRepository: CompanyIdentifierRepository

    @Autowired
    private lateinit var isinLeiRepository: IsinLeiRepository

    @Autowired
    private lateinit var companyQueryManager: CompanyQueryManager

    companion object {
        @Container
        @JvmStatic
        val postgresContainer =
            PostgreSQLContainer("postgres:15")
                .withDatabaseName("dataland_test")
                .withUsername("test")
                .withPassword("test")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.jpa.show-sql") { "false" }
            registry.add("spring.jpa.properties.hibernate.format_sql") { "false" }
            registry.add("spring.flyway.enabled") { "false" }
        }
    }

    private val originalLei = listOf("ORIGINAL123456789012")
    private val originalDuns = listOf("123456789")
    private val originalIsin = listOf("DE0001234567", "US1234567890")
    private val originalCompanyName = "Original Company Name"
    private val originalHeadquarters = "Original Headquarters"
    private val originalAlternativeNames = listOf("Original Alt Name")
    private val originalSectorCodeWz = "6201"
    private val originalSector = "Technology"
    private val originalCompanyContactDetails = listOf("original@company.com")

    private lateinit var existingCompany: StoredCompanyEntity

    @BeforeEach
    fun setup() {
        val companyInformation =
            CompanyInformation(
                companyName = originalCompanyName,
                companyAlternativeNames = originalAlternativeNames,
                companyContactDetails = originalCompanyContactDetails,
                companyLegalForm = "AG",
                headquarters = originalHeadquarters,
                headquartersPostalCode = "10115",
                sector = originalSector,
                sectorCodeWz = originalSectorCodeWz,
                identifiers =
                    mapOf(
                        IdentifierType.Lei to originalLei,
                        IdentifierType.Duns to originalDuns,
                        IdentifierType.Isin to originalIsin,
                    ),
                countryCode = "DE",
                isTeaserCompany = false,
                website = "https://original-company.com",
                parentCompanyLei = "PARENT123456789012",
            )

        existingCompany = companyAlterationManager.addCompany(companyInformation)
    }

    @Test
    fun `patchCompany should correctly update basic company information while preserving original values if null is provided`() {
        val newName = "Updated Company Name"
        val newHeadquarters = "New Headquarters"
        val newAlternativeNames = listOf("Updated Alt Name 1", "Updated Alt Name 2")
        val newCompnyContactDetails = listOf("new@company.com", "another_new@company.com")
        val patch =
            CompanyInformationPatch(
                companyName = newName,
                headquarters = newHeadquarters,
                companyAlternativeNames = newAlternativeNames,
                companyContactDetails = newCompnyContactDetails,
                sectorCodeWz = null,
            )

        val updatedCompany = companyAlterationManager.patchCompany(existingCompany.companyId, patch)

        assertEquals(newName, updatedCompany.companyName)
        assertEquals(newHeadquarters, updatedCompany.headquarters)
        assertEquals(newAlternativeNames, updatedCompany.companyAlternativeNames)
        assertNotNull(updatedCompany.sectorCodeWz)

        assertEquals(originalSectorCodeWz, updatedCompany.sectorCodeWz)
        assertEquals(originalSector, updatedCompany.sector)
    }

    @Test
    fun `patchCompany should correctly update company identifiers`() {
        val updatedLei = listOf("UPDATED123456789012")
        val updatedDuns = listOf("987654321", "111222333")
        val updatedIsin = listOf("FR0001234567")
        val patch =
            CompanyInformationPatch(
                identifiers =
                    mapOf(
                        IdentifierType.Lei to updatedLei,
                        IdentifierType.Duns to updatedDuns,
                        IdentifierType.Isin to updatedIsin,
                    ),
            )
        companyAlterationManager.patchCompany(existingCompany.companyId, patch)

        val isinLeiContent = isinLeiRepository.findAll()
        assertEquals(updatedIsin.size, isinLeiContent.size)
        assertEquals(updatedLei.first(), isinLeiContent.first().lei)
        assertEquals(updatedIsin.first(), isinLeiContent.first().isin)

        val companyIdentifierContent = companyIdentifierRepository.findAll()
        assertEquals(updatedLei.size + updatedDuns.size, companyIdentifierContent.size)

        val retrievedCompany = companyQueryManager.getCompanyApiModelById(existingCompany.companyId)
        val identifierMap = retrievedCompany.companyInformation.identifiers

        assertEquals(updatedLei.size, identifierMap[IdentifierType.Lei]?.size)
        assertEquals(updatedLei, identifierMap[IdentifierType.Lei])

        assertEquals(updatedDuns.size, identifierMap[IdentifierType.Duns]?.size)
        assertTrue(identifierMap[IdentifierType.Duns]?.containsAll(updatedDuns) ?: false)

        assertEquals(updatedIsin.size, identifierMap[IdentifierType.Isin]?.size)
        assertEquals(updatedIsin, identifierMap[IdentifierType.Isin])
    }

    @Test
    fun `patchCompany should update ISIN identifiers while preserving existing LEI when LEI not provided in patch`() {
        val updatedIsin = listOf("GB0001234567", "NL0001234567")
        val patch =
            CompanyInformationPatch(
                identifiers =
                    mapOf(
                        IdentifierType.Isin to updatedIsin,
                    ),
            )

        companyAlterationManager.patchCompany(existingCompany.companyId, patch)

        val isinLeiContent = isinLeiRepository.findAll()
        assertEquals(updatedIsin.size, isinLeiContent.size)
        isinLeiContent.forEach {
            assertTrue(updatedIsin.contains(it.isin))
            assertEquals(originalLei.first(), it.lei)
        }

        val retrievedCompany = companyQueryManager.getCompanyApiModelById(existingCompany.companyId)
        val identifierMap = retrievedCompany.companyInformation.identifiers

        assertEquals(updatedIsin.size, identifierMap[IdentifierType.Isin]?.size)
        assertTrue(identifierMap[IdentifierType.Isin]?.containsAll(updatedIsin) ?: false)
    }

    @Test
    fun `check that creating or patching a company with existing identifiers throws an exception`() {
        val newLei = listOf("NEW123456789012")
        val newMinimalCompany =
            CompanyInformation(
                companyName = "New Company",
                headquarters = "New Town",
                identifiers =
                    mapOf(
                        IdentifierType.Lei to originalLei,
                    ),
                countryCode = "DE",
            )

        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.addCompany(newMinimalCompany)
        }
        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.addCompany(newMinimalCompany.copy(identifiers = mapOf(IdentifierType.Isin to originalIsin)))
        }
        val newCompany = companyAlterationManager.addCompany(newMinimalCompany.copy(identifiers = mapOf(IdentifierType.Lei to newLei)))
        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.patchCompany(newCompany.companyId, patch = newMinimalCompany.toCompanyInformationPatch())
        }
    }
}
