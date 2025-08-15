package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.TestPostgresContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [DatalandBackend::class])
@Testcontainers
class IsinLeiManagerTest(
    @Autowired private val storedCompanyRepository: StoredCompanyRepository,
    @Autowired private val isinLeiRepository: IsinLeiRepository,
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
    @Autowired private val isinLeiTransactionalService: IsinLeiTransactionalService,
) {
    // Even though this class uses a test container for integration testing, it is not possible to use the BaseIntegrationTest class.
    // The use of @Async does not work with the @Transactional and @Rollback annotations in the BaseIntegrationTest class.
    companion object {
        @Container
        @JvmStatic
        val postgres = TestPostgresContainer.postgres

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            TestPostgresContainer.configureProperties(registry)
        }
    }

    val dummyLei1 = "LEI123456789"
    val dummyLei2 = "LEI987654321"
    val dummyIsin1 = "123456789"
    val dummyIsin2 = "987654321"

    val payload =
        listOf(
            IsinLeiMappingData(
                isin = dummyIsin1,
                lei = dummyLei1,
            ),
            IsinLeiMappingData(
                isin = dummyIsin2,
                lei = dummyLei2,
            ),
        )
    lateinit var dummyCompany1: StoredCompanyEntity
    lateinit var dummyCompany2: StoredCompanyEntity

    val companyWithTestLei1 =
        CompanyInformation(
            companyName = "Test Company 1",
            countryCode = "DE",
            headquarters = "Berlin",
            headquartersPostalCode = "8",
            identifiers = mapOf(IdentifierType.Lei to listOf(dummyLei1)),
        )
    val companyWithTestLei2 =
        CompanyInformation(
            companyName = "Test Company 2",
            countryCode = "DE",
            headquarters = "Berlin",
            headquartersPostalCode = "8",
            identifiers = mapOf(IdentifierType.Lei to listOf(dummyLei2)),
        )

    lateinit var isinLeiManager: IsinLeiManager

    @BeforeEach
    fun setup() {
        isinLeiRepository.deleteAll()
        companyIdentifierRepository.deleteAll()
        storedCompanyRepository.deleteAll()
        isinLeiManager =
            IsinLeiManager(
                storedCompanyRepository = storedCompanyRepository,
                isinLeiTransactionalService = isinLeiTransactionalService,
                isinLeiRepository = isinLeiRepository,
            )
        dummyCompany1 = companyAlterationManager.addCompany(companyWithTestLei1)
        dummyCompany2 = companyAlterationManager.addCompany(companyWithTestLei2)
    }

    @Test
    fun `add sample ISIN LEI mapping to empty database and check if it is there`() {
        isinLeiManager.postIsinLeiMapping(payload)

        val result = isinLeiRepository.findAll().toList().sortedBy { it.isin }
        assertEquals(2, result.size)
        assertEquals(dummyIsin1, result[0].isin)
        assertEquals(dummyCompany1.companyId, result[0].company.companyId)
        assertEquals(dummyIsin2, result[1].isin)
        assertEquals(dummyCompany2.companyId, result[1].company.companyId)
    }

    @Test
    fun `add sample ISIN LEI mapping to database and check if it replaced the old data`() {
        val entity1 = IsinLeiEntity(dummyCompany1, "123", "LEI123")
        val entity2 = IsinLeiEntity(dummyCompany2, "456", "LEI456")
        isinLeiRepository.saveAllAndFlush(listOf(entity1, entity2))
        val resultBefore = isinLeiRepository.findAll().toList().sortedBy { it.isin }
        assertEquals(2, resultBefore.size)
        assertEquals("123", resultBefore[0].isin)
        assertEquals("456", resultBefore[1].isin)

        isinLeiManager.postIsinLeiMapping(payload)

        val result = isinLeiRepository.findAll().toList().sortedBy { it.isin }
        assertEquals(2, result.size)
        assertEquals(dummyIsin1, result[0].isin)
        assertEquals(dummyCompany1.companyId, result[0].company.companyId)
        assertEquals(dummyIsin2, result[1].isin)
        assertEquals(dummyCompany2.companyId, result[1].company.companyId)
    }
}
