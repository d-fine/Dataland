package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.AssembledDataManager
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.JsonTestUtils.testObjectMapper
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.model.StorableDataPoint
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.Optional

class AssembledDataManagerTest {
    private val dataManager = mock(DataManager::class.java)
    private val metaDataManager = mock(DataPointMetaInformationManager::class.java)
    private val storageClient = mock(StorageControllerApi::class.java)
    private val messageQueuePublications = mock(MessageQueuePublications::class.java)
    private val dataPointValidator = mock(DataPointValidator::class.java)
    private val companyQueryManager = mock(CompanyQueryManager::class.java)
    private val companyRoleChecker = mock(CompanyRoleChecker::class.java)
    private val logMessageBuilder = mock(LogMessageBuilder::class.java)
    private val specificationClient = mock(SpecificationControllerApi::class.java)
    private val datasetDatapointRepository = mock(DatasetDatapointRepository::class.java)

    private val inputFrameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"
    private val inputData = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val currencyDataPoint = "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReference.json"

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, testObjectMapper, logMessageBuilder,
        )

    private val spyDataPointManager = spy(dataPointManager)

    private val assembledDataManager =
        AssembledDataManager(
            dataManager, messageQueuePublications, dataPointValidator, testObjectMapper,
            specificationClient, datasetDatapointRepository, spyDataPointManager, ReferencedReportsUtilities(testObjectMapper),
        )

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val reportingPeriod = "2022"
    private val companyId = "test-company-id"
    private val datasetId = "test-dataset-id"
    private val dataPointType = "extendedEnumFiscalYearDeviation"
    private val dataPointId = "test-data-point-1"
    private val frameworkSpecification = TestResourceFileReader.getKotlinObject<FrameworkSpecification>(inputFrameworkSpecification)
    private val framework = "sfdr"
    private val dataDimensions = BasicDataDimensions(companyId, framework, reportingPeriod)

    @BeforeEach
    fun resetMocks() {
        reset(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, logMessageBuilder, specificationClient, datasetDatapointRepository,
        )
    }

    @BeforeEach
    fun setGeneralMocks() {
        `when`(specificationClient.getFrameworkSpecification(any())).thenReturn(frameworkSpecification)
    }

    @Test
    fun `check that processing a dataset works as expected`() {
        val expectedDataPointTypes = listOf("extendedEnumFiscalYearDeviation", "extendedDateFiscalYearEnd", "extendedCurrencyEquity")
        val inputData = TestResourceFileReader.getJsonString(inputData)

        `when`(specificationClient.getFrameworkSpecification(any())).thenReturn(frameworkSpecification)

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
        verify(messageQueuePublications, times(expectedDataPointTypes.size)).publishDataPointUploadedMessage(any(), any(), eq(null), any())
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
        `when`(metaDataManager.getCurrentlyActiveDataId(dataPointDimensions)).thenReturn(dataPointId)
        setMockData(dataPointMap, dataContentMap)

        val dynamicDataset = assembledDataManager.getDatasetData(dataDimensions, correlationId)
        assert(!dynamicDataset.isNullOrEmpty())
        assert(dynamicDataset!!.contains(dataPoint))
        assert(dynamicDataset.contains("\"referencedReports\":{\"ESEFReport\":"))
    }

    @Test
    fun `check that an exception is thrown if no data exists for the dynamic dataset`() {
        `when`(metaDataManager.getCurrentlyActiveDataId(any())).thenReturn(null)

        assertThrows<ResourceNotFoundApiException> {
            assembledDataManager.getDatasetData(dataDimensions, correlationId)
        }
    }

    private fun setMockData(
        dataPoints: Map<String, String>,
        dataContent: Map<String, String>,
    ) {
        `when`(datasetDatapointRepository.findById(datasetId)).thenReturn(
            Optional.of(
                DatasetDatapointEntity(
                    datasetId = datasetId,
                    dataPoints = dataPoints,
                ),
            ),
        )

        `when`(metaDataManager.getDataPointMetaInformationById(any())).thenAnswer { invocation ->
            val dataPointId = invocation.getArgument<String>(0)
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

        `when`(storageClient.selectDataPointById(any(), any())).thenAnswer { invocation ->
            val dataPointId = invocation.getArgument<String>(0)
            StorableDataPoint(
                dataPoint = dataContent[dataPointId] ?: "",
                dataPointType = dataPoints.filterValues { it == dataPointId }.keys.first(),
                companyId = companyId,
                reportingPeriod = reportingPeriod,
            )
        }
    }
}
