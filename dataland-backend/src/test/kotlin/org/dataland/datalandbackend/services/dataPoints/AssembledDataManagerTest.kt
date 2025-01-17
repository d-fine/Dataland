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
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.model.StorableDataPoint
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecificationDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
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

    private val frameworkSpecificationFile = "./json/frameworkTemplate/frameworkSpecification.json"
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
    private val reportingPeriod = "test-period"
    private val companyId = "test-company-id"
    private val datasetId = "test-dataset-id"
    private val frameworkSpecification = TestResourceFileReader.getKotlinObject<FrameworkSpecificationDto>(frameworkSpecificationFile)

    @BeforeEach
    fun resetMocks() {
        reset(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, logMessageBuilder, specificationClient, datasetDatapointRepository,
        )
    }

    @Test
    fun `check that processing a dataset works as expected`() {
        val expectedDataPointIdentifiers = listOf("extendedEnumFiscalYearDeviation", "extendedDateFiscalYearEnd", "extendedCurrencyEquity")
        val inputData = TestResourceFileReader.getJsonString(inputData)

        `when`(specificationClient.getFrameworkSpecification(any())).thenReturn(frameworkSpecification)

        val uploadedDataset =
            StorableDataset(
                companyId = companyId,
                dataType = DataType("sfdr"),
                uploaderUserId = uploaderUserId,
                uploadTime = Instant.now().toEpochMilli(),
                reportingPeriod = reportingPeriod,
                data = inputData,
            )

        assembledDataManager.storeDataset(uploadedDataset, false, correlationId)
        expectedDataPointIdentifiers.forEach {
            verify(spyDataPointManager, times(1)).storeDataPoint(
                argThat { dataPointIdentifier == it }, any(), any(), any(),
            )
        }
        verify(messageQueuePublications, times(expectedDataPointIdentifiers.size)).publishDataPointUploadedMessage(any(), any(), any())
        verify(messageQueuePublications, times(1)).publishDatasetQaRequiredMessage(any(), any(), any())
        verify(messageQueuePublications, times(0)).publishDatasetUploadedMessage(any(), any(), any())
        verify(datasetDatapointRepository, times(1)).save(
            argThat {
                dataPoints.keys.sorted() == expectedDataPointIdentifiers.sorted()
            },
        )
    }

    @Test
    fun `check that assembling a dataset works as expected`() {
        val dataPointMap = mapOf("extendedEnumFiscalYearDeviation" to "test-data-point-1", "extendedCurrencyEquity" to "test-data-point-2")

        val dataPointContent =
            listOf(
                "{\"content\":\"test-content-1\"}",
                TestResourceFileReader.getJsonString(currencyDataPoint),
            )

        val dataContentMap =
            mapOf(
                "test-data-point-1" to dataPointContent[0],
                "test-data-point-2" to dataPointContent[1],
            )

        `when`(specificationClient.getFrameworkSpecification(any())).thenReturn(frameworkSpecification)

        `when`(datasetDatapointRepository.findById(datasetId)).thenReturn(
            Optional.of(
                DatasetDatapointEntity(
                    datasetId = datasetId,
                    dataPoints = dataPointMap,
                ),
            ),
        )

        `when`(metaDataManager.getDataPointMetaInformationByDataId(any())).thenAnswer { invocation ->
            val dataId = invocation.getArgument<String>(0)
            DataPointMetaInformationEntity(
                dataId = dataId,
                companyId = companyId,
                dataPointIdentifier = dataPointMap.filterValues { it == dataId }.keys.first(),
                reportingPeriod = reportingPeriod,
                uploaderUserId = uploaderUserId,
                uploadTime = Instant.now().toEpochMilli(),
                currentlyActive = true,
                qaStatus = QaStatus.Accepted,
            )
        }

        `when`(storageClient.selectDataPointById(any(), any())).thenAnswer { invocation ->
            val dataId = invocation.getArgument<String>(0)
            StorableDataPoint(
                dataPointContent = dataContentMap[dataId] ?: "",
                dataPointIdentifier = dataPointMap.filterValues { it == dataId }.keys.first(),
                companyId = companyId,
                reportingPeriod = reportingPeriod,
            )
        }

        val assembledDataset = assembledDataManager.getDatasetData(datasetId, "sfdr", correlationId)
        dataPointContent.forEach {
            assert(assembledDataset.contains(it))
        }
        assert(assembledDataset.contains("\"referencedReports\":{\"ESEFReport\":"))
    }
}
