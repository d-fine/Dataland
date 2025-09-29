package org.dataland.datalandbackend.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.TestPostgresContainer
import org.dataland.datalandbackendutils.model.BasicDataSetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@SpringBootTest(classes = [DatalandBackend::class])
@Testcontainers
@Transactional
@Rollback
class DataAvailabilityCheckerTest {
    companion object {
        private const val REPORTING_PERIOD = "2023"
        private const val DATA_TYPE = "sfdr"
        private const val COMPANY_ID = "46b5374b-a720-43e6-9c5e-9dd92bd95b33"

        // Even though this class uses a test container for integration testing, it is not possible to use the BaseIntegrationTest class.
        // This is due to the direct usage of the EntityManager, which will lead to issues connecting to the database
        @Container
        @JvmStatic
        val postgres = TestPostgresContainer.postgres

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            TestPostgresContainer.configureProperties(registry)
        }
    }

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var dataMetaInformationRepository: DataMetaInformationRepository

    @Autowired
    private lateinit var dataCompositionService: DataCompositionService

    @Autowired
    private lateinit var storedCompanyRepository: StoredCompanyRepository
    private lateinit var dataAvailabilityChecker: DataAvailabilityChecker

    @MockitoBean
    private var specificationClient = mock<SpecificationControllerApi>()

    private val baseDimension = BasicDataSetDimensions(companyId = COMPANY_ID, framework = DATA_TYPE, reportingPeriod = REPORTING_PERIOD)

    private val dummyCompany =
        StoredCompanyEntity(
            companyId = COMPANY_ID,
            companyName = "Test Company",
            countryCode = "DE",
            headquarters = "Berlin",
            identifiers = mutableListOf(),
            dataRegisteredByDataland = mutableListOf(),
            isTeaserCompany = false,
            website = null,
            parentCompanyLei = null,
            companyAlternativeNames = null,
            companyContactDetails = null,
            sector = null,
            sectorCodeWz = null,
            companyLegalForm = null,
            headquartersPostalCode = null,
            associatedSubdomains = null,
        )

    @BeforeEach
    fun setUp() {
        dataAvailabilityChecker = DataAvailabilityChecker(entityManager, dataCompositionService)
        storedCompanyRepository.saveAndFlush(dummyCompany)
        whenever(specificationClient.listFrameworkSpecifications()).thenReturn(
            listOf(SimpleFrameworkSpecification(IdWithRef(DATA_TYPE, "dummy"), "Test Framework")),
        )
    }

    @ParameterizedTest
    @CsvSource(
        "1234, $DATA_TYPE, $REPORTING_PERIOD",
        "$COMPANY_ID, dummy, $REPORTING_PERIOD",
        "$COMPANY_ID, $DATA_TYPE, 12345",
    )
    fun `check that data dimensions are filtered out correctly`(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ) {
        storeMetaData(dataType = dataType, reportingPeriod = reportingPeriod)
        val results =
            dataAvailabilityChecker.getMetaDataOfActiveDatasets(
                listOf(
                    BasicDataSetDimensions(
                        companyId = companyId,
                        framework = dataType,
                        reportingPeriod = reportingPeriod,
                    ),
                ),
            )
        assert(results.isEmpty())
    }

    @Test
    fun `check that the availability check returns active datasets as expected`() {
        storeMetaData()
        storeMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.getMetaDataOfActiveDatasets(listOf(baseDimension))
        assert(results.size == 1) { "There should be exactly one result." }
        val resultingDimensions = results.map { BasicDataSetDimensions(it.companyId, it.dataType.toString(), it.reportingPeriod) }
        assert(resultingDimensions.first() == baseDimension) { "The result should be the provided example." }
    }

    @Test
    fun `check that multiple dimensions are retrieved correctly`() {
        val year = "2024"
        val framework = "lksg"
        storeMetaData()
        storeMetaData(currentlyActive = null)
        storeMetaData(reportingPeriod = year)
        storeMetaData(dataType = framework)
        storeMetaData(dataType = framework, reportingPeriod = year, currentlyActive = false)

        val expectedDimensions =
            listOf(
                baseDimension,
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = DATA_TYPE, reportingPeriod = year),
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = framework, reportingPeriod = REPORTING_PERIOD),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = framework, reportingPeriod = year),
                BasicDataSetDimensions(companyId = UUID.randomUUID().toString(), framework = DATA_TYPE, reportingPeriod = REPORTING_PERIOD),
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = DATA_TYPE, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.getMetaDataOfActiveDatasets(expectedDimensions + unexpectedDimensions)

        assert(results.size == expectedDimensions.size) { "Incorrect number of datasets found." }
        val resultingDimensions = results.map { BasicDataSetDimensions(it.companyId, it.dataType.toString(), it.reportingPeriod) }
        assert(expectedDimensions.containsAll(resultingDimensions))
    }

    private fun storeMetaData(
        dataType: String? = DATA_TYPE,
        reportingPeriod: String? = REPORTING_PERIOD,
        currentlyActive: Boolean? = true,
    ) {
        dataMetaInformationRepository.saveAndFlush(
            DataMetaInformationEntity(
                dataId = UUID.randomUUID().toString(),
                company = dummyCompany,
                dataType = dataType ?: DATA_TYPE,
                reportingPeriod = reportingPeriod ?: REPORTING_PERIOD,
                currentlyActive = currentlyActive,
                uploadTime = System.currentTimeMillis(),
                qaStatus = QaStatus.Accepted,
                uploaderUserId = UUID.randomUUID().toString(),
            ),
        )
    }
}
