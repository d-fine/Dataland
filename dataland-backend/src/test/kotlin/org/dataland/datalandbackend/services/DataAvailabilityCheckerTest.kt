package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.repositories.DataMetaInformationRepository
import org.dataland.datalandbackend.repositories.DataPointMetaInformationRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.utils.DataAvailabilityIgnoredFieldsUtils
import org.dataland.datalandbackend.utils.DataBaseCreationUtils
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.services.utils.BaseIntegrationTest
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import org.dataland.datalandbackend.utils.DEFAULT_COMPANY_ID as companyId
import org.dataland.datalandbackend.utils.DEFAULT_DATA_POINT_TYPE as dataPointType
import org.dataland.datalandbackend.utils.DEFAULT_FRAMEWORK as framework
import org.dataland.datalandbackend.utils.DEFAULT_REPORTING_PERIOD as reportingPeriod

@SpringBootTest(
    classes = [DatalandBackend::class],
    properties = ["spring.rabbitmq.listener.simple.auto-startup=false"],
)
@DefaultMocks
class DataAvailabilityCheckerTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var dataMetaInformationRepository: DataMetaInformationRepository

    @Autowired
    private lateinit var dataPointMetaInformationRepository: DataPointMetaInformationRepository

    @Autowired
    private lateinit var storedCompanyRepository: StoredCompanyRepository

    @Autowired
    private lateinit var dataAvailabilityChecker: DataAvailabilityChecker

    @Autowired
    private lateinit var specificationClient: SpecificationControllerApi

    private lateinit var dbCreationUtils: DataBaseCreationUtils

    private val datasetDimension = BasicDataDimensions(companyId = companyId, dataType = framework, reportingPeriod = reportingPeriod)
    private val dataPointDimension = BasicDataDimensions(companyId = companyId, dataType = dataPointType, reportingPeriod = reportingPeriod)

    @BeforeEach
    fun setUp() {
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

    @Test
    fun `filterViewableDimensions with list - empty input returns empty result`() {
        val results = dataAvailabilityChecker.filterViewableDimensions(emptyList())
        assert(results.isEmpty()) { "Empty input should return empty result." }
    }

    @Test
    fun `filterViewableDimensions with list - active dataset is returned`() {
        dbCreationUtils.storeDatasetMetaData(currentlyActive = true)
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.filterViewableDimensions(listOf(datasetDimension))
        assert(results.size == 1) { "There should be exactly one result." }
        assert(results.first() == datasetDimension) { "The result should be the provided dimension." }
    }

    @Test
    fun `filterViewableDimensions with list - active data point is returned`() {
        dbCreationUtils.storeDataPointMetaData(currentlyActive = true)
        dbCreationUtils.storeDataPointMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.filterViewableDimensions(listOf(dataPointDimension))
        assert(results.size == 1) { "There should be exactly one result." }
        assert(results.first() == dataPointDimension) { "The result should be the provided dimension." }
    }

    @Test
    fun `filterViewableDimensions with list - active dataset and active data point are both returned`() {
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDataPointMetaData()
        val results = dataAvailabilityChecker.filterViewableDimensions(listOf(datasetDimension, dataPointDimension))
        assert(results.size == 2) { "Both dimensions should be returned." }
        assert(results.containsAll(listOf(datasetDimension, dataPointDimension))) { "Both dimensions should be in the result." }
    }

    @ParameterizedTest
    @CsvSource(
        "1234, $framework, $reportingPeriod",
        "$companyId, unknowntype, $reportingPeriod",
        "$companyId, $framework, 12345",
    )
    fun `filterViewableDimensions with list - invalid dimensions are filtered out`(
        testCompanyId: String,
        dataType: String,
        testReportingPeriod: String,
    ) {
        dbCreationUtils.storeDatasetMetaData(dataType = dataType, reportingPeriod = testReportingPeriod)
        val results =
            dataAvailabilityChecker.filterViewableDimensions(
                listOf(BasicDataDimensions(companyId = testCompanyId, dataType = dataType, reportingPeriod = testReportingPeriod)),
            )
        assert(results.isEmpty()) { "Invalid dimensions should be filtered out." }
    }

    @Test
    fun `check that the availability check returns active datasets as expected`() {
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        val results = dataAvailabilityChecker.filterViewableDimensions(listOf(datasetDimension))
        assert(results.size == 1) { "There should be exactly one result." }
        assert(results.first() == datasetDimension) { "The result should be the provided example." }
    }

    @Test
    fun `filterViewableDimensions with list - multiple dimensions mix of active inactive and non-existent`() {
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
                BasicDataDimensions(companyId = companyId, dataType = otherFramework, reportingPeriod = otherYear),
                BasicDataDimensions(companyId = companyId, dataType = otherFramework, reportingPeriod = reportingPeriod),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataDimensions(companyId = companyId, dataType = otherFramework, reportingPeriod = otherYear),
                BasicDataDimensions(companyId = otherId, dataType = otherFramework, reportingPeriod = reportingPeriod),
                BasicDataDimensions(companyId = companyId, dataType = otherFramework, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.filterViewableDimensions(expectedDimensions + unexpectedDimensions)
        assert(results.size == expectedDimensions.size) { "Incorrect number of dimensions found." }
        assert(expectedDimensions.containsAll(results)) { "Unexpected dimensions in result." }
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
                BasicDataDimensions(companyId = companyId, dataType = anotherDataPointType, reportingPeriod = anotherYear),
                BasicDataDimensions(companyId = companyId, dataType = anotherDataPointType, reportingPeriod = reportingPeriod),
            )

        val unexpectedDimensions =
            listOf(
                BasicDataDimensions(companyId = companyId, dataType = anotherDataPointType, reportingPeriod = anotherYear),
                BasicDataDimensions(companyId = anotherId, dataType = anotherDataPointType, reportingPeriod = reportingPeriod),
                BasicDataDimensions(companyId = companyId, dataType = anotherDataPointType, reportingPeriod = "2020"),
            )

        val results = dataAvailabilityChecker.filterViewableDimensions(expectedDimensions + unexpectedDimensions)

        assert(results.size == expectedDimensions.size) { "Incorrect number of data points found." }
        val resultingDimensions = results.map { BasicDataDimensions(it.companyId, it.dataType, it.reportingPeriod) }
        assert(expectedDimensions.containsAll(resultingDimensions))
    }

    @Test
    fun `searchViewableDimensions with filters - filter matching active dataset returns correct dimensions`() {
        dbCreationUtils.storeDatasetMetaData()
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { "There should be exactly one result." }
        assert(results.first() == datasetDimension) { "The result should match the stored dataset dimension." }
    }

    @Test
    fun `searchViewableDimensions with filters - filter matching active data point returns correct dimensions`() {
        dbCreationUtils.storeDataPointMetaData()
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(dataPointType),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { "There should be exactly one result." }
        assert(results.first() == dataPointDimension) { "The result should match the stored data point dimension." }
    }

    @Test
    fun `searchViewableDimensions with filters - no matching data returns empty result`() {
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.isEmpty()) { "No matching data should return empty result." }
    }

    @Test
    fun `searchViewableDimensions with filters - inactive data is excluded`() {
        dbCreationUtils.storeDatasetMetaData(currentlyActive = null)
        dbCreationUtils.storeDatasetMetaData(currentlyActive = false)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.isEmpty()) { "Inactive data should be excluded." }
    }

    @Test
    fun `searchViewableDimensions with filters - empty lists act as wildcards`() {
        dbCreationUtils.storeDatasetMetaData()
        dbCreationUtils.storeDataPointMetaData()
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = emptyList(),
                    reportingPeriods = emptyList(),
                ),
            )
        assert(results.containsAll(listOf(datasetDimension, dataPointDimension))) { "Both dimensions should be in the result." }
    }

    @Test
    fun `searchViewableDimensions with filters - multiple frameworks filter returns only matching framework`() {
        val otherFramework = "lksg"
        dbCreationUtils.storeDatasetMetaData(dataType = framework)
        dbCreationUtils.storeDatasetMetaData(dataType = otherFramework)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { "Only one framework's data should be returned." }
        assert(results.first() == datasetDimension) { "Only the filtered framework should be in the result." }
    }

    @Test
    fun `searchViewableDimensions with filters - multiple periods filter returns only matching period`() {
        val otherPeriod = "2024"
        dbCreationUtils.storeDatasetMetaData(reportingPeriod = reportingPeriod)
        dbCreationUtils.storeDatasetMetaData(reportingPeriod = otherPeriod)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 1) { "Only one period's data should be returned." }
        assert(results.first() == datasetDimension) { "Only the filtered period should be in the result." }
    }

    @Test
    fun `searchViewableDimensions with filters - mixed datasets and data points with cross-cutting filter`() {
        dbCreationUtils.storeDatasetMetaData(dataType = framework)
        dbCreationUtils.storeDataPointMetaData(dataPointType = dataPointType)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(framework, dataPointType),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.size == 2) { "Both dataset and data point dimensions should be returned." }
        assert(results.containsAll(listOf(datasetDimension, dataPointDimension))) { "Both dimensions should be in the result." }
    }

    @Test
    fun `searchViewableDimensions - data points with only ignored fields do not yield a dimension`() {
        val ignoredDataPointType = DataAvailabilityIgnoredFieldsUtils.getIgnoredFields().first()
        doReturn(null).whenever(specificationClient).getDataPointTypeSpecification(ignoredDataPointType)
        dbCreationUtils.storeDataPointMetaData(dataPointType = ignoredDataPointType)
        val results =
            dataAvailabilityChecker.searchViewableDimensions(
                DataDimensionQuery(
                    companyIds = listOf(companyId),
                    dataTypes = listOf(ignoredDataPointType),
                    reportingPeriods = listOf(reportingPeriod),
                ),
            )
        assert(results.none { it.dataType == framework }) { "Ignored-only data points must not produce a framework dimension." }
    }
}
