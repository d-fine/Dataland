package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.exceptions.DuplicateIdentifierApiException
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.companies.CompanyInformationPatch
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.utils.TestPostgresContainer
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
        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            TestPostgresContainer.configureProperties(registry)
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
    private val originalCompany =
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
    private val newLei = listOf("NEW123456789012")
    private val newName = "New Company Name"
    private val newHeadquarters = "New Town"
    private val newCountryCode = "CH"
    private val newMinimalCompany =
        CompanyInformation(
            companyName = newName,
            headquarters = newHeadquarters,
            identifiers = mapOf(IdentifierType.Lei to originalLei),
            countryCode = newCountryCode,
        )

    private lateinit var existingCompany: StoredCompanyEntity

    @BeforeEach
    fun setup() {
        existingCompany = companyAlterationManager.addCompany(originalCompany)
    }

    @Test
    fun `patchCompany should correctly update basic company information while preserving original values if null is provided`() {
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
        val patch =
            CompanyInformationPatch(
                companyName = newName,
                headquarters = newHeadquarters,
                identifiers = mapOf(IdentifierType.Lei to originalLei),
                countryCode = newCountryCode,
            )

        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.addCompany(newMinimalCompany)
        }
        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.addCompany(newMinimalCompany.copy(identifiers = mapOf(IdentifierType.Isin to originalIsin)))
        }
        val newCompany = companyAlterationManager.addCompany(newMinimalCompany.copy(identifiers = mapOf(IdentifierType.Lei to newLei)))
        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.patchCompany(newCompany.companyId, patch = patch)
        }
    }

    @Test
    fun `check that putting a company works as expected`() {
        val putCompany = companyAlterationManager.putCompany(existingCompany.companyId, newMinimalCompany)
        assertEquals(newMinimalCompany.companyName, putCompany.companyName)
        assertEquals(newMinimalCompany.headquarters, putCompany.headquarters)
        assertEquals(1, putCompany.identifiers.size)
        assertEquals(IdentifierType.Lei, putCompany.identifiers.first().identifierType)
        assertEquals(originalLei.first(), putCompany.identifiers.first().identifierValue)
        assertEquals(newMinimalCompany.countryCode, putCompany.countryCode)
        assertEquals(null, putCompany.sector)
    }

    @Test
    fun `check that adding a company twice leads to the expected error message`() {
        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.addCompany(originalCompany)
        }
    }
}
