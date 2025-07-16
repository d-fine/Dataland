package org.dataland.datalandbackend.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import javax.sql.DataSource

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class IsinLeiManagerTest(
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
    @Autowired private val isinLeiRepository: IsinLeiRepository,
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val dataSource: DataSource,
) {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    val dummyLei1 = "LEI123456789"
    val dummyLei2 = "LEI987654321"
    val dummyIsin1 = "123456789"
    val dummyIsin2 = "987654321"
    val entity1 = IsinLeiEntity("123", "123", "LEI123")
    val entity2 = IsinLeiEntity("456", "456", "LEI456")
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
    lateinit var dummyCompanyId1: String
    lateinit var dummyCompanyId2: String

    val companyWithTestLei1 =
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
                    IdentifierType.Lei to listOf(dummyLei1),
                ),
            parentCompanyLei = null,
        )
    val companyWithTestLei2 =
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
                    IdentifierType.Lei to listOf(dummyLei2),
                ),
            parentCompanyLei = null,
        )

    lateinit var isinLeiManager: IsinLeiManager

    @BeforeEach
    fun setup() {
        isinLeiManager =
            IsinLeiManager(
                companyIdentifierRepository = companyIdentifierRepository,
                dataSource = dataSource,
                dataSourceMaximumPoolSize = 50,
            )
        dummyCompanyId1 = companyAlterationManager.addCompany(companyWithTestLei1).companyId
        dummyCompanyId2 = companyAlterationManager.addCompany(companyWithTestLei2).companyId
    }

    @Test
    fun `add sample ISIN LEI mapping to empty database and check if it is there`() {
        isinLeiManager.putIsinLeiMapping(payload)

        val result = isinLeiRepository.findAll().toList().sortedBy { it.isin }
        assertEquals(2, result.size)
        assertEquals(dummyIsin1, result[0].isin)
        assertEquals(dummyCompanyId1, result[0].companyId)
        assertEquals(dummyIsin2, result[1].isin)
        assertEquals(dummyCompanyId2, result[1].companyId)
    }

    @Test
    fun `add sample ISIN LEI mapping to database and check if it replaced the old data`() {
        entityManager.persist(entity1)
        entityManager.persist(entity2)
        entityManager.flush()

        isinLeiManager.putIsinLeiMapping(payload)

        val result = isinLeiRepository.findAll().toList().sortedBy { it.isin }
        assertEquals(2, result.size)
        assertEquals(dummyIsin1, result[0].isin)
        assertEquals(dummyCompanyId1, result[0].companyId)
        assertEquals(dummyIsin2, result[1].isin)
        assertEquals(dummyCompanyId2, result[1].companyId)
    }
}
