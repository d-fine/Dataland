package org.dataland.datalandbackend.services

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.DataAvailabilityIgnoredFieldsUtils
import org.dataland.datalandbackend.utils.DataBaseCreationUtils
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.services.utils.TestPostgresContainer
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
import org.dataland.datalandbackend.utils.DEFAULT_COMPANY_ID as companyId
import org.dataland.datalandbackend.utils.DEFAULT_DATA_POINT_TYPE as dataPointType
import org.dataland.datalandbackend.utils.DEFAULT_FRAMEWORK as framework
import org.dataland.datalandbackend.utils.DEFAULT_REPORTING_PERIOD as reportingPeriod

@SpringBootTest(classes = [DatalandBackend::class])
@Testcontainers
@Transactional
@Rollback
class DataAvailabilityCheckerTest {
    companion object {
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
    private lateinit var dataPointMetaInformationRepository: DataPointMetaInformationRepository

    @Autowired
    private lateinit var dataCompositionService: DataCompositionService

    @Autowired
    private lateinit var storedCompanyRepository: StoredCompanyRepository
    private lateinit var dataAvailabilityChecker: DataAvailabilityChecker

    @MockitoBean
    private var specificationClient = mock<SpecificationControllerApi>()

    private lateinit var dbCreationUtils: DataBaseCreationUtils

    private val datasetDimension = BasicDatasetDimensions(companyId = companyId, framework = framework, reportingPeriod = reportingPeriod)
    private val dataPointDimension =
        BasicDataPointDimensions(companyId = companyId, dataPointType = dataPointType, reportingPeriod = reportingPeriod)
    private val brokenCompanyId =
        BasicDataPointDimensions(companyId = "1234", dataPointType = dataPointType, reportingPeriod = reportingPeriod)
    private val brokenReportingPeriod =
        BasicDataPointDimensions(companyId = companyId, dataPointType = dataPointType, reportingPeriod = "0")
    private val allDimensions = listOf(dataPointDimension, brokenCompanyId, brokenReportingPeriod)
//ToDo update object
    @BeforeEach
    fun setUp() {
        dataAvailabilityChecker = DataAvailabilityChecker(entityManager, dataCompositionService)
        whenever(specificationClient.listFrameworkSpecifications()).thenReturn(
            listOf(SimpleFrameworkSpecification(IdWithRef(framework, "dummy"), "Test Framework")),
        )
        doReturn(null).whenever(specificationClient).getDataPointTypeSpecification(dataPointType)
        dbCreationUtils =
            DataBaseCreationUtils(
                storedCompanyRepository = storedCompanyRepository,
                dataMetaInformationRepository = dataMetaInformationRepository,
                dataPointMetaInformationRepository = dataPointMetaInformationRepository,
            )
    }

    @ParameterizedTest
    @CsvSource(
        "1234, $framework, $reportingPeriod",
        "$companyId, dummy, $reportingPeriod",
        "$companyId, $framework, 12345",
    )
    fun `check that data dimensions are filtered out correctly`(
        companyId: String,
        dataType: String,
        reportingPeriod: String,
    ) {
        dbCreationUtils.storeDatasetMetaData(dataType = dataType, reportingPeriod = reportingPeriod)
        val results =
            dataAvailabilityChecker.getMetaDataOfActiveDatasets(
                listOf(
                    BasicDatasetDimensions(
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
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.getMetaDataOfActiveDatasets(listOf(datasetDimension))
        assert(results.size == 1) { "There should be exactly one result." }
        val resultingDimensions = results.map { BasicDatasetDimensions(it.companyId, it.dataType.toString(), it.reportingPeriod) }
        assert(resultingDimensions.first() == datasetDimension) { "The result should be the provided example." }
    }

    @Test
    fun `check that multiple dimensions are retrieved correctly`() {
        val otherYear = "2024"
        val otherFramework = "lksg"
        val otherId = UUID.randomUUID().toString()
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        dbCreationUtils.storeDatasetMetaData(dataType = otherFramework, reportingPeriod = otherYear)
        dbCreationUtils.storeDatasetMetaData(dataType = otherFramework)
        dbCreationUtils.storeDatasetMetaData(dataType = otherFramework, reportingPeriod = otherYear, currentlyActive = false)

        val expectedDimensions =
            listOf(
                datasetDimension,
                BasicDatasetDimensions(companyId = companyId, framework = otherFramework, reportingPeriod = otherYear),
                BasicDatasetDimensions(companyId = companyId, framework = otherFramework, reportingPeriod = reportingPeriod),
            )

        val unexpectedDimensions =
            listOf(
                BasicDatasetDimensions(companyId = companyId, framework = otherFramework, reportingPeriod = otherYear),
                BasicDatasetDimensions(companyId = otherId, framework = otherFramework, reportingPeriod = reportingPeriod),
                BasicDatasetDimensions(companyId = companyId, framework = otherFramework, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.getMetaDataOfActiveDatasets(expectedDimensions + unexpectedDimensions)

        assert(results.size == expectedDimensions.size) { "Incorrect number of datasets found." }
        val resultingDimensions = results.map { BasicDatasetDimensions(it.companyId, it.dataType.toString(), it.reportingPeriod) }
        assert(expectedDimensions.containsAll(resultingDimensions))
    }

    @Test
    fun `check that the availability check returns active data points as expected`() {
        dbCreationUtils.storeDataPointMetaData()
        dbCreationUtils.storeDataPointMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.getMetaDataOfActiveDataPoints(allDimensions)
        assert(results.size == 1) { "There should be exactly one result." }
        val resultingDimensions = results.map { BasicDataPointDimensions(it.companyId, it.dataPointType, it.reportingPeriod) }
        assert(resultingDimensions.first() == dataPointDimension) { "The result should be the provided example." }
    }

    @Test
    fun `check that multiple data point dimensions are retrieved correctly`() {
        val anotherYear = "2024"
        val anotherDataPointType = "anotherDataPoint"
        val anotherId = UUID.randomUUID().toString()
        dbCreationUtils.storeDataPointMetaData()
        dbCreationUtils.storeDataPointMetaData(currentlyActive = null)
        dbCreationUtils.storeDataPointMetaData(dataPointType = anotherDataPointType, reportingPeriod = anotherYear)
        dbCreationUtils.storeDataPointMetaData(dataPointType = anotherDataPointType)
        dbCreationUtils.storeDataPointMetaData(dataPointType = anotherDataPointType, reportingPeriod = anotherYear, currentlyActive = false)

        val expectedDimensions =
            listOf(
                dataPointDimension,
                BasicDataPointDimensions(companyId = companyId, dataPointType = anotherDataPointType, reportingPeriod = anotherYear),
                BasicDataPointDimensions(companyId = companyId, dataPointType = anotherDataPointType, reportingPeriod = reportingPeriod),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataPointDimensions(companyId = companyId, dataPointType = anotherDataPointType, reportingPeriod = anotherYear),
                BasicDataPointDimensions(companyId = anotherId, dataPointType = anotherDataPointType, reportingPeriod = reportingPeriod),
                BasicDataPointDimensions(companyId = companyId, dataPointType = anotherDataPointType, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.getMetaDataOfActiveDataPoints(expectedDimensions + unexpectedDimensions)

        assert(results.size == expectedDimensions.size) { "Incorrect number of data points found." }
        val resultingDimensions = results.map { BasicDataPointDimensions(it.companyId, it.dataPointType, it.reportingPeriod) }
        assert(expectedDimensions.containsAll(resultingDimensions))
    }

    @Test
    fun `check that only broken and inactive dimensions lead to an empty result`() {
        dbCreationUtils.storeDataPointMetaData(currentlyActive = false)
        val results = dataAvailabilityChecker.getMetaDataOfActiveDataPoints(allDimensions)
        assert(results.isEmpty()) { "There should be no result." }
    }

    @Test
    fun `check that datasets with only ignored fields are not delivered`() {
        val ignoredDimensions =
            BasicDataPointDimensions(
                companyId = companyId,
                dataPointType = DataAvailabilityIgnoredFieldsUtils.getIgnoredFields().first(),
                reportingPeriod = reportingPeriod,
            )
        dbCreationUtils.storeDataPointMetaData(dataPointType = ignoredDimensions.dataPointType)
        val results = dataAvailabilityChecker.getViewableDataPointIds(listOf(ignoredDimensions, dataPointDimension))
        assert(results.isEmpty()) { "There should be no result." }
    }
}
