package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.metainformation.PlainDataAndMetaInformation
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataAvailabilityChecker
import org.dataland.datalandbackend.services.DataCompositionService
import org.dataland.datalandbackend.services.DataDeliveryService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.InternalStorageAdapter
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.SpecificationService
import org.dataland.datalandbackend.services.datapoints.AssembledDataManager
import org.dataland.datalandbackend.services.datapoints.DataPointCalculator
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.model.StorableDataPoint
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

class AssembledDataManagerTest {
    private val dataManager = mock<DataManager>()
    private val metaDataManager = mock<DataPointMetaInformationManager>()
    private val storageClient = mock<StorageControllerApi>()
    private val messageQueuePublications = mock<MessageQueuePublications>()
    private val dataPointValidator = mock<DataPointValidator>()
    private val companyQueryManager = mock<CompanyQueryManager>()
    private val companyRoleChecker = mock<CompanyRoleChecker>()
    private val logMessageBuilder = mock<LogMessageBuilder>()
    private val specificationClient = mock<SpecificationControllerApi>()
    private val datasetDatapointRepository = mock<DatasetDatapointRepository>()
    private val dataAvailabilityChecker = mock<DataAvailabilityChecker>()

    private val inputFrameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"
    private val inputSimpleFrameworkSpecification = "./json/frameworkTemplate/simpleFrameworkSpecification.json"
    private val inputCalculatedFrameworkSpecification = "./json/frameworkTemplate/frameworkSpecificationCalculation.json"
    private val inputData = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val currencyDataPoint = "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReference.json"
    private val numericDataPoint = "./json/frameworkTemplate/numericDataPointWithExtendedDocumentReference.json"
    private val calculatedDataPointSpec = "./json/specifications/dataPointWithCalculation.json"

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, defaultObjectMapper, logMessageBuilder,
        )

    private val referencedReportsUtilities = ReferencedReportsUtilities()
    private lateinit var datasetAssembler: DatasetAssembler
    private lateinit var dataCompositionService: DataCompositionService
    private lateinit var dataDeliveryService: DataDeliveryService
    private lateinit var assembledDataManager: AssembledDataManager
    private lateinit var specificationService: SpecificationService
    private lateinit var dataPointUtils: DataPointUtils
    private lateinit var internalStorageAdapter: InternalStorageAdapter
    private lateinit var dataPointCalculator: DataPointCalculator

    private val spyDataPointManager = spy(dataPointManager)
    private val testDataProvider = TestDataProvider(defaultObjectMapper)

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val reportingPeriod = "2022"
    private val companyId = "test-company-id"
    private val datasetId = "test-dataset-id"
    private val dataPointType = "extendedEnumFiscalYearDeviationDummy"
    private val dataPointId = "test-data-point-1"
    private val frameworkSpecification = TestResourceFileReader.getKotlinObject<FrameworkSpecification>(inputFrameworkSpecification)
    private val simpleFrameworkSpecification =
        TestResourceFileReader
            .getKotlinObject<SimpleFrameworkSpecification>(inputSimpleFrameworkSpecification)
    private val calculatedFrameworkSpecification =
        TestResourceFileReader
            .getKotlinObject<FrameworkSpecification>(inputCalculatedFrameworkSpecification)
    private val framework = "sfdr"
    private val dataDimensions = BasicDatasetDimensions(companyId, framework, reportingPeriod)

    private fun makeStubSpec(dataPointType: String) =
        DataPointTypeSpecification(
            dataPointType = IdWithRef(id = dataPointType, ref = ""),
            name = dataPointType,
            businessDefinition = "",
            dataPointBaseType = IdWithRef(id = "extendedDecimal", ref = ""),
            usedBy = emptyList(),
        )

    @BeforeEach
    fun resetMocks() {
        reset(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, logMessageBuilder, specificationClient, datasetDatapointRepository,
        )
    }

    @BeforeEach
    fun setSpecificationMocks() {
        doReturn(frameworkSpecification).whenever(specificationClient).getFrameworkSpecification(any())
        doAnswer { invocation ->
            val dataPointType = invocation.getArgument<String>(0)
            if (dataPointType == framework) {
                throw ClientException()
            }
            makeStubSpec(dataPointType)
        }.whenever(specificationClient).getDataPointTypeSpecification(any())
        doReturn(listOf(simpleFrameworkSpecification)).whenever(specificationClient).listFrameworkSpecifications()
        specificationService = SpecificationService(specificationClient)
        specificationService.initiateSpecifications(null)
        dataCompositionService = DataCompositionService(specificationService)
        datasetAssembler = DatasetAssembler(specificationService, referencedReportsUtilities)
        internalStorageAdapter = InternalStorageAdapter(storageClient)
        dataPointCalculator =
            DataPointCalculator(
                dataCompositionService,
                dataAvailabilityChecker,
                internalStorageAdapter,
                specificationService,
                metaDataManager,
            )
        dataPointUtils = DataPointUtils(specificationClient, metaDataManager, dataCompositionService, dataPointCalculator)
        dataDeliveryService =
            DataDeliveryService(
                dataCompositionService, dataAvailabilityChecker,
                internalStorageAdapter, datasetAssembler, dataPointCalculator,
            )
        assembledDataManager =
            AssembledDataManager(
                dataManager, messageQueuePublications, dataPointValidator,
                datasetDatapointRepository, spyDataPointManager,
                referencedReportsUtilities,
                companyQueryManager, dataPointUtils, dataDeliveryService, datasetAssembler, specificationService,
            )
    }

    @Test
    fun `check that processing a dataset works as expected`() {
        val expectedDataPointTypes =
            listOf("extendedEnumFiscalYearDeviationDummy", "extendedDateFiscalYearEnd", "extendedCurrencyEquity")
        val inputData = TestResourceFileReader.getJsonString(inputData)

        doReturn(testDataProvider.getEmptyStoredCompanyEntity()).whenever(companyQueryManager).getCompanyById(any())

        val uploadedDataset =
            StorableDataset(
                companyId = companyId,
                dataType = DataType(framework),
                uploaderUserId = uploaderUserId,
                uploadTime = Instant.now().toEpochMilli(),
                reportingPeriod = reportingPeriod,
                data = inputData,
            )

        assembledDataManager.storeDataset(uploadedDataset, false, correlationId)

        expectedDataPointTypes.forEach {
            verify(spyDataPointManager, times(1)).storeDataPoint(
                argThat { dataPointType == it }, any(), any(), any(), any(),
            )
        }
        verify(messageQueuePublications, times(expectedDataPointTypes.size))
            .publishDataPointUploadedMessage(any(), any(), any(), any())
        verify(messageQueuePublications, times(1)).publishDatasetQaRequiredMessage(any(), any(), any())
        verify(messageQueuePublications, times(0)).publishDatasetUploadedMessage(any(), any(), any())
        verify(datasetDatapointRepository, times(1)).save(
            argThat {
                dataPoints.keys.sorted() == expectedDataPointTypes.sorted()
            },
        )
    }

    @Test
    fun `check that assembling a dataset works as expected`() {
        val dataPointMap = mapOf(dataPointType to dataPointId, "extendedCurrencyEquity" to "test-data-point-2")

        val dataPoints =
            listOf(
                "{\"content\":\"test-content-1\"}",
                TestResourceFileReader.getJsonString(currencyDataPoint),
            )

        val dataContentMap =
            mapOf(
                dataPointId to dataPoints[0],
                "test-data-point-2" to dataPoints[1],
            )

        setMockData(dataPointMap, dataContentMap)

        val assembledDataset = assembledDataManager.getDatasetData(datasetId, framework, correlationId)
        dataPoints.forEach {
            assert(assembledDataset.contains(it))
        }
        assert(assembledDataset.contains("\"referencedReports\":{\"ESEFReport\":"))
    }

    @Test
    fun `check that assembling a dynamic dataset works as expected`() {
        val dataPointMap = mapOf(dataPointType to dataPointId)
        val dataPoint = TestResourceFileReader.getJsonString(currencyDataPoint)
        val dataContentMap = mapOf(dataPointId to dataPoint)
        val dataPointDimensions = BasicDataPointDimensions(companyId, dataPointType, reportingPeriod)
        setMockData(dataPointMap, dataContentMap)
        doReturn(listOf(dataPointId)).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())

        val dynamicDataset =
            assertDoesNotThrow {
                assembledDataManager.getDatasetData(setOf(dataDimensions), correlationId)[dataDimensions]
            }
        assert(!dynamicDataset.isNullOrEmpty())
        assert(dynamicDataset!!.contains(dataPoint))
        assert(dynamicDataset.contains("\"referencedReports\":{\"ESEFReport\":"))
    }

    @Test
    fun `check that exceptions are thrown only in the expected cases in the context of dynamic datasets`() {
        doReturn(null).whenever(metaDataManager).getCurrentlyActiveDataId(any())

        assertDoesNotThrow {
            assembledDataManager.getDatasetData(setOf(dataDimensions), correlationId)
        }

        val invalidCompanyId = "invalid-company-id"
        doThrow(ResourceNotFoundApiException("dummy", "dummy"))
            .whenever(companyQueryManager)
            .assertCompanyIdExists(invalidCompanyId)

        val searchFilter =
            DataMetaInformationSearchFilter(
                companyId = companyId,
                dataType = DataType(framework),
                onlyActive = true,
            )

        assertThrows<ResourceNotFoundApiException> {
            assembledDataManager.getAllDatasetsAndMetaInformation(
                searchFilter = searchFilter.copy(companyId = invalidCompanyId),
                correlationId = correlationId,
            )
        }

        assertThrows<IllegalArgumentException> {
            assembledDataManager.getAllDatasetsAndMetaInformation(
                searchFilter = searchFilter.copy(companyId = null),
                correlationId = correlationId,
            )
        }

        assertDoesNotThrow {
            assertEquals(
                emptyList<PlainDataAndMetaInformation>(),
                assembledDataManager.getAllDatasetsAndMetaInformation(searchFilter, correlationId),
            )
        }
    }

    @Test
    fun `check that a dataset containing calculated fields is correctly delivered`() {
        val sourceOneType = "extendedDecimalScope1GhgEmissionsInTonnes"
        val sourceTwoType = "extendedDecimalScope2GhgEmissionsInTonnes"
        val resultType = "extendedDecimalScope1And2GhgEmissionsInTonnes"
        val sourceOneId = "Id1"
        val sourceTwoId = "Id2"

        doReturn(calculatedFrameworkSpecification).whenever(specificationClient).getFrameworkSpecification(any())

        val dataPointMap = mapOf(sourceOneType to sourceOneId, sourceTwoType to sourceTwoId)
        val dataPointSpec = TestResourceFileReader.getKotlinObject<DataPointTypeSpecification>(calculatedDataPointSpec)
        val dataPoint = TestResourceFileReader.getJsonString(numericDataPoint)
        val dataContentMap = mapOf(sourceOneId to dataPoint, sourceTwoId to dataPoint)
        val dataPointDimensions = BasicDataPointDimensions(companyId, resultType, reportingPeriod)
        doReturn(listOf(sourceOneId, sourceTwoId)).whenever(dataAvailabilityChecker).getViewableDataPointIds(any())
        doReturn(dataPointSpec).whenever(specificationClient).getDataPointTypeSpecification(resultType)
        setMockData(dataPointMap, dataContentMap)
        val dynamicDataset =
            assertDoesNotThrow {
                assembledDataManager.getDatasetData(setOf(dataDimensions), correlationId)[dataDimensions]
            }
        assertNotNull(dynamicDataset)
        assertTrue(dynamicDataset.isNotEmpty())
        val assembledDatasetNode = defaultObjectMapper.readTree(dynamicDataset)
        val calculatedDataPointNode =
            assembledDatasetNode
                .path("environmental")
                .path("greenhouseGasEmissions")
                .path("scope1And2GhgEmissionsInTonnes")
        assertFalse(calculatedDataPointNode.isMissingNode) {
            "Expected calculated data point 'scope1And2GhgEmissionsInTonnes' to be present in the assembled dataset"
        }
        assertEquals(0, BigDecimal("1.0").compareTo(calculatedDataPointNode.path("value").decimalValue()))
    }

    private fun setMockData(
        dataPoints: Map<String, String>,
        dataContent: Map<String, String>,
    ) {
        doReturn(
            Optional.of(
                DatasetDatapointEntity(
                    datasetId = datasetId,
                    dataPoints = dataPoints,
                ),
            ),
        ).whenever(datasetDatapointRepository).findById(datasetId)

        doAnswer { invocation ->
            val dataPointId = invocation.getArgument<Collection<String>>(0)
            dataPointId.map { dataPointId ->
                DataPointMetaInformationEntity(
                    dataPointId = dataPointId,
                    companyId = companyId,
                    dataPointType = dataPoints.filterValues { it == dataPointId }.keys.first(),
                    reportingPeriod = reportingPeriod,
                    uploaderUserId = uploaderUserId,
                    uploadTime = Instant.now().toEpochMilli(),
                    currentlyActive = true,
                    qaStatus = QaStatus.Accepted,
                )
            }
        }.whenever(metaDataManager).getDataPointMetaInformationByIds(any())

        doAnswer { invocation ->
            val dimensionsByDataset =
                invocation.getArgument<Map<BasicDatasetDimensions, List<BasicDataPointDimensions>>>(0)
            dimensionsByDataset.mapValues { (_, dimensions) ->
                dimensions.mapNotNull { dimension ->
                    dataPoints[dimension.dataPointType]?.let { dataPointId ->
                        DataPointMetaInformationEntity(
                            dataPointId = dataPointId,
                            companyId = dimension.companyId,
                            dataPointType = dimension.dataPointType,
                            reportingPeriod = dimension.reportingPeriod,
                            uploaderUserId = uploaderUserId,
                            uploadTime = Instant.now().toEpochMilli(),
                            currentlyActive = true,
                            qaStatus = QaStatus.Accepted,
                        )
                    }
                }
            }
        }.whenever(dataAvailabilityChecker)
            .getViewableDataPointMetaData(any<Map<BasicDatasetDimensions, List<BasicDataPointDimensions>>>())

        doAnswer { invocation ->
            val dataPointId = invocation.getArgument<List<String>>(1)
            dataPointId.associateWith { dataPointId ->
                StorableDataPoint(
                    dataPoint = dataContent[dataPointId] ?: "",
                    dataPointType = dataPoints.filterValues { it == dataPointId }.keys.first(),
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                )
            }
        }.whenever(storageClient).selectBatchDataPointsByIds(any(), any())
    }
}
