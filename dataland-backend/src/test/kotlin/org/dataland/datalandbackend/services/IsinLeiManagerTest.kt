package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.model.companies.CompanyInformation
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class IsinLeiManagerTest(
    @Autowired private val storedCompanyRepository: StoredCompanyRepository,
    @Autowired private val isinLeiRepository: IsinLeiRepository,
    @Autowired private val companyAlterationManager: CompanyAlterationManager,
    @Autowired private val dataSource: DataSource,
) {
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
            companyName = "Test Company 2",
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
                storedCompanyRepository = storedCompanyRepository,
                dataSource = dataSource,
                dataSourceMaximumPoolSize = 50,
            )
        dummyCompany1 = companyAlterationManager.addCompany(companyWithTestLei1)
        dummyCompany2 = companyAlterationManager.addCompany(companyWithTestLei2)
    }

    @Test
    fun `add sample ISIN LEI mapping to empty database and check if it is there`() {
        isinLeiManager.putIsinLeiMapping(payload)

        val result = isinLeiRepository.findAll().toList().sortedBy { it.isin }
        assertEquals(2, result.size)
        assertEquals(dummyIsin1, result[0].isin)
        assertEquals(dummyCompany1.companyId, result[0].company?.companyId)
        assertEquals(dummyIsin2, result[1].isin)
        assertEquals(dummyCompany2.companyId, result[1].company?.companyId)
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

        isinLeiManager.putIsinLeiMapping(payload)

        val result = isinLeiRepository.findAll().toList().sortedBy { it.isin }
        assertEquals(2, result.size)
        assertEquals(dummyIsin1, result[0].isin)
        assertEquals(dummyCompany1.companyId, result[0].company?.companyId)
        assertEquals(dummyIsin2, result[1].isin)
        assertEquals(dummyCompany2.companyId, result[1].company?.companyId)
    }
}
