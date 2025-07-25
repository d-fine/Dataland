package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.CompanyIdentifierEntity
import org.dataland.datalandbackend.entities.CompanyIdentifierEntityId
import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.exceptions.DuplicateIdentifierApiException
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

class CompanyAlterationManagerTest {
    private val mockStoredCompanyRepository = mock<StoredCompanyRepository>()
    private val mockCompanyIdentifierRepository = mock<CompanyIdentifierRepository>()
    private val mockCompanyQueryManager = mock<CompanyQueryManager>()
    private val mockIsinLeiRepository = mock<IsinLeiRepository>()
    private val mockStoredCompanyEntity = mock<StoredCompanyEntity>()
    private lateinit var companyAlterationManager: CompanyAlterationManager

    private val dummyLei1 = "dummyLei1"
    private val dummyLei2 = "dummyLei2"
    private val knownLei = "knownLei"
    private val dummyIsin1 = "dummyIsin1"
    private val dummyIsin2 = "dummyIsin2"
    private val knownIsin = "knownIsin"
    private val dummyDuns = "dummyDuns"

    private fun getDummyCompanyInformation(
        identifierMapping: Map<IdentifierType, List<String>> = mapOf(IdentifierType.Lei to listOf(dummyLei1)),
    ): CompanyInformation =
        CompanyInformation(
            companyName = "Dummy Company",
            companyAlternativeNames = null,
            companyContactDetails = null,
            companyLegalForm = null,
            headquarters = "Dummy Town",
            headquartersPostalCode = null,
            sector = "Dummy Sector",
            sectorCodeWz = null,
            identifiers = identifierMapping,
            countryCode = "DE",
            isTeaserCompany = false,
            website = null,
            parentCompanyLei = null,
        )

    @BeforeEach
    fun setup() {
        reset(
            mockStoredCompanyRepository,
            mockCompanyIdentifierRepository,
            mockCompanyQueryManager,
            mockIsinLeiRepository,
        )

        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockStoredCompanyRepository).save(any())
        doAnswer { invocation -> invocation.arguments[0] }
            .whenever(mockCompanyIdentifierRepository)
            .saveAllAndFlush<CompanyIdentifierEntity>(any())
        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockIsinLeiRepository).saveAllAndFlush<IsinLeiEntity>(any())

        doReturn(
            listOf(
                CompanyIdentifierEntity(
                    identifierType = IdentifierType.Lei,
                    identifierValue = knownLei,
                    company = mockStoredCompanyEntity,
                ),
            ),
        ).whenever(mockCompanyIdentifierRepository).findAllById(
            listOf(
                CompanyIdentifierEntityId(
                    identifierType = IdentifierType.Lei,
                    identifierValue = knownLei,
                ),
            ),
        )

        doReturn(
            listOf(
                IsinLeiEntity(
                    company = mockStoredCompanyEntity,
                    isin = knownIsin,
                    lei = knownLei,
                ),
            ),
        ).whenever(mockIsinLeiRepository).findAllByIsinIn(listOf(knownIsin))

        companyAlterationManager =
            CompanyAlterationManager(
                mockStoredCompanyRepository,
                mockCompanyIdentifierRepository,
                mockCompanyQueryManager,
                mockIsinLeiRepository,
            )
    }

    @Test
    fun `check that a new company cannot be posted with the LEI of another company`() {
        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.addCompany(
                getDummyCompanyInformation(mapOf(IdentifierType.Lei to listOf(knownLei))),
            )
        }
    }

    @Test
    fun `check that a new company cannot be posted with more than one LEI`() {
        assertThrows<InvalidInputApiException> {
            companyAlterationManager.addCompany(
                getDummyCompanyInformation(mapOf(IdentifierType.Lei to listOf(dummyLei1, dummyLei2))),
            )
        }
    }

    @Test
    fun `check that a new company cannot be posted with an ISIN of another company`() {
        assertThrows<DuplicateIdentifierApiException> {
            companyAlterationManager.addCompany(
                getDummyCompanyInformation(
                    mapOf(
                        IdentifierType.Lei to listOf(dummyLei1),
                        IdentifierType.Isin to listOf(knownIsin),
                    ),
                ),
            )
        }
    }

    @Test
    fun `check that saved identifiers are listed in the response after posting a new company`() {
        val storedCompanyEntity =
            assertDoesNotThrow {
                companyAlterationManager.addCompany(
                    getDummyCompanyInformation(
                        mapOf(
                            IdentifierType.Lei to listOf(dummyLei1),
                            IdentifierType.Duns to listOf(dummyDuns),
                            IdentifierType.Isin to listOf(dummyIsin1, dummyIsin2),
                        ),
                    ),
                )
            }
        assertEquals(4, storedCompanyEntity.identifiers.size)
        assert(
            storedCompanyEntity.identifiers.containsAll(
                listOf(
                    CompanyIdentifierEntity(
                        identifierType = IdentifierType.Lei, identifierValue = dummyLei1, company = storedCompanyEntity, isNew = true,
                    ),
                    CompanyIdentifierEntity(
                        identifierType = IdentifierType.Duns, identifierValue = dummyDuns, company = storedCompanyEntity, isNew = true,
                    ),
                    CompanyIdentifierEntity(
                        identifierType = IdentifierType.Isin, identifierValue = dummyIsin1, company = storedCompanyEntity, isNew = true,
                    ),
                    CompanyIdentifierEntity(
                        identifierType = IdentifierType.Isin, identifierValue = dummyIsin2, company = storedCompanyEntity, isNew = true,
                    ),
                ),
            ),
        )
    }
}
