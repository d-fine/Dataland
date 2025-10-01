package org.dataland.datalandbackend.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.TestPostgresContainer
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDataSetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.doReturn
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
        private const val BASE_YEAR = "2023"
        private const val FRAMEWORK = "sfdr"
        private const val COMPANY_ID = "46b5374b-a720-43e6-9c5e-9dd92bd95b33"
        private const val DATA_POINT_TYPE = "testDataPoint"

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
    private lateinit var dataMetaPointInformationRepository: DataPointMetaInformationRepository

    @Autowired
    private lateinit var dataCompositionService: DataCompositionService

    @Autowired
    private lateinit var storedCompanyRepository: StoredCompanyRepository
    private lateinit var dataAvailabilityChecker: DataAvailabilityChecker

    @MockitoBean
    private var specificationClient = mock<SpecificationControllerApi>()

    private val datasetDimension = BasicDataSetDimensions(companyId = COMPANY_ID, framework = FRAMEWORK, reportingPeriod = BASE_YEAR)

    private val dataPointDimension =
        BasicDataPointDimensions(companyId = COMPANY_ID, dataPointType = DATA_POINT_TYPE, reportingPeriod = BASE_YEAR)
    private val brokenCompanyId =
        BasicDataPointDimensions(companyId = "1234", dataPointType = DATA_POINT_TYPE, reportingPeriod = BASE_YEAR)
    private val brokenReportingPeriod =
        BasicDataPointDimensions(companyId = COMPANY_ID, dataPointType = DATA_POINT_TYPE, reportingPeriod = "0")
    private val allDimensions = listOf(dataPointDimension, brokenCompanyId, brokenReportingPeriod)

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
            listOf(SimpleFrameworkSpecification(IdWithRef(FRAMEWORK, "dummy"), "Test Framework")),
        )
        doReturn(null).whenever(specificationClient).getDataPointTypeSpecification(DATA_POINT_TYPE)
    }

    @ParameterizedTest
    @CsvSource(
        "1234, $FRAMEWORK, $BASE_YEAR",
        "$COMPANY_ID, dummy, $BASE_YEAR",
        "$COMPANY_ID, $FRAMEWORK, 12345",
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
        val results = dataAvailabilityChecker.getMetaDataOfActiveDatasets(listOf(datasetDimension))
        assert(results.size == 1) { "There should be exactly one result." }
        val resultingDimensions = results.map { BasicDataSetDimensions(it.companyId, it.dataType.toString(), it.reportingPeriod) }
        assert(resultingDimensions.first() == datasetDimension) { "The result should be the provided example." }
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
                datasetDimension,
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = FRAMEWORK, reportingPeriod = year),
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = framework, reportingPeriod = BASE_YEAR),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = framework, reportingPeriod = year),
                BasicDataSetDimensions(companyId = UUID.randomUUID().toString(), framework = FRAMEWORK, reportingPeriod = BASE_YEAR),
                BasicDataSetDimensions(companyId = COMPANY_ID, framework = FRAMEWORK, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.getMetaDataOfActiveDatasets(expectedDimensions + unexpectedDimensions)

        assert(results.size == expectedDimensions.size) { "Incorrect number of datasets found." }
        val resultingDimensions = results.map { BasicDataSetDimensions(it.companyId, it.dataType.toString(), it.reportingPeriod) }
        assert(expectedDimensions.containsAll(resultingDimensions))
    }

    @Test
    fun `check that the availability check returns active data points as expected`() {
        storeDataPointMetaData()
        storeDataPointMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.getMetaDataOfActiveDataPoints(allDimensions)
        assert(results.size == 1) { "There should be exactly one result." }
        val resultingDimensions = results.map { BasicDataPointDimensions(it.companyId, it.dataPointType, it.reportingPeriod) }
        assert(resultingDimensions.first() == dataPointDimension) { "The result should be the provided example." }
    }

    @Test
    fun `check that multiple data point dimensions are retrieved correctly`() {
        val anotherYear = "2024"
        val dataPointType = "anotherDataPoint"
        val anotherId = UUID.randomUUID().toString()
        storeDataPointMetaData()
        storeDataPointMetaData(currentlyActive = null)
        storeDataPointMetaData(reportingPeriod = anotherYear)
        storeDataPointMetaData(dataPointType = dataPointType)
        storeDataPointMetaData(dataPointType = dataPointType, reportingPeriod = anotherYear, currentlyActive = false)

        val expectedDimensions =
            listOf(
                dataPointDimension,
                BasicDataPointDimensions(companyId = COMPANY_ID, dataPointType = DATA_POINT_TYPE, reportingPeriod = anotherYear),
                BasicDataPointDimensions(companyId = COMPANY_ID, dataPointType = dataPointType, reportingPeriod = BASE_YEAR),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataPointDimensions(companyId = COMPANY_ID, dataPointType = dataPointType, reportingPeriod = anotherYear),
                BasicDataPointDimensions(companyId = anotherId, dataPointType = DATA_POINT_TYPE, reportingPeriod = BASE_YEAR),
                BasicDataPointDimensions(companyId = COMPANY_ID, dataPointType = DATA_POINT_TYPE, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.getMetaDataOfActiveDataPoints(expectedDimensions + unexpectedDimensions)

        assert(results.size == expectedDimensions.size) { "Incorrect number of data points found." }
        val resultingDimensions = results.map { BasicDataPointDimensions(it.companyId, it.dataPointType, it.reportingPeriod) }
        assert(expectedDimensions.containsAll(resultingDimensions))
    }

    @Test
    fun `check that only broken and inactive dimensions lead to an empty result`() {
        storeDataPointMetaData(currentlyActive = false)
        val results = dataAvailabilityChecker.getMetaDataOfActiveDataPoints(allDimensions)
        assert(results.isEmpty()) { "There should be no result." }
    }

    private fun storeMetaData(
        dataType: String? = FRAMEWORK,
        reportingPeriod: String? = BASE_YEAR,
        currentlyActive: Boolean? = true,
    ) {
        dataMetaInformationRepository.saveAndFlush(
            DataMetaInformationEntity(
                dataId = UUID.randomUUID().toString(),
                company = dummyCompany,
                dataType = dataType ?: FRAMEWORK,
                reportingPeriod = reportingPeriod ?: BASE_YEAR,
                currentlyActive = currentlyActive,
                uploadTime = System.currentTimeMillis(),
                qaStatus = QaStatus.Accepted,
                uploaderUserId = UUID.randomUUID().toString(),
            ),
        )
    }

    private fun storeDataPointMetaData(
        dataPointType: String? = DATA_POINT_TYPE,
        reportingPeriod: String? = BASE_YEAR,
        currentlyActive: Boolean? = true,
    ) {
        dataMetaPointInformationRepository.saveAndFlush(
            DataPointMetaInformationEntity(
                dataPointId = UUID.randomUUID().toString(),
                companyId = dummyCompany.companyId,
                dataPointType = dataPointType ?: DATA_POINT_TYPE,
                reportingPeriod = reportingPeriod ?: BASE_YEAR,
                currentlyActive = currentlyActive,
                uploadTime = System.currentTimeMillis(),
                qaStatus = QaStatus.Accepted,
                uploaderUserId = UUID.randomUUID().toString(),
            ),
        )
    }
}
