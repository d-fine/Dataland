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
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.AssembledDataManager
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
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.dataland.specificationservice.openApiClient.model.SimpleFrameworkSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
    private val inputData = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val currencyDataPoint = "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReference.json"

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, defaultObjectMapper, logMessageBuilder,
        )

    private val referencedReportsUtilities = ReferencedReportsUtilities()
    private val datasetAssembler = DatasetAssembler(specificationClient, referencedReportsUtilities)
    private lateinit var dataCompositionService: DataCompositionService
    private lateinit var dataDeliveryService: DataDeliveryService
    private lateinit var assembledDataManager: AssembledDataManager
    private val dataPointUtils = DataPointUtils(defaultObjectMapper, specificationClient, metaDataManager)

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
    private val framework = "sfdr"
    private val dataDimensions = BasicDatasetDimensions(companyId, framework, reportingPeriod)

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
        doThrow(ClientException()).whenever(specificationClient).getDataPointTypeSpecification(framework)
        doReturn(listOf(simpleFrameworkSpecification)).whenever(specificationClient).listFrameworkSpecifications()
        dataCompositionService = DataCompositionService(specificationClient)
        dataDeliveryService = DataDeliveryService(dataCompositionService, dataAvailabilityChecker, storageClient, datasetAssembler)
        assembledDataManager =
            AssembledDataManager(
                dataManager, messageQueuePublications, dataPointValidator, defaultObjectMapper,
                datasetDatapointRepository, spyDataPointManager,
                referencedReportsUtilities,
                companyQueryManager, dataPointUtils, dataDeliveryService, datasetAssembler,
            )
        dataCompositionService.initiateSpecifications(null)
    }

    @Test
    fun `check that processing a dataset works as expected`() {
        val expectedDataPointTypes =
            listOf("extendedEnumFiscalYearDeviationDummy", "extendedDateFiscalYearEnd", "extendedCurrencyEquity")
        val inputData = TestResourceFileReader.getJsonString(inputData)

        whenever(companyQueryManager.getCompanyById(any())).thenReturn(testDataProvider.getEmptyStoredCompanyEntity())

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
        whenever(metaDataManager.getCurrentlyActiveDataId(dataPointDimensions)).thenReturn(dataPointId)
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

    private fun setMockData(
        dataPoints: Map<String, String>,
        dataContent: Map<String, String>,
    ) {
        whenever(datasetDatapointRepository.findById(datasetId)).thenReturn(
            Optional.of(
                DatasetDatapointEntity(
                    datasetId = datasetId,
                    dataPoints = dataPoints,
                ),
            ),
        )

        whenever(metaDataManager.getDataPointMetaInformationByIds(any())).thenAnswer { invocation ->
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
        }

        whenever(storageClient.selectBatchDataPointsByIds(any(), any())).thenAnswer { invocation ->
            val dataPointId = invocation.getArgument<List<String>>(1)
            dataPointId.associateWith { dataPointId ->
                StorableDataPoint(
                    dataPoint = dataContent[dataPointId] ?: "",
                    dataPointType = dataPoints.filterValues { it == dataPointId }.keys.first(),
                    companyId = companyId,
                    reportingPeriod = reportingPeriod,
                )
            }
        }
    }
}
