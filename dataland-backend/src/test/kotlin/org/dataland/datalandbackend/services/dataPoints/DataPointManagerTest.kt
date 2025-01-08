package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.JsonOperations.objectMapper
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.Optional

class DataPointManagerTest {
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

    private val frameworkSpecification = "./json/frameworkTemplate/frameworkSpecification.json"
    private val inputData = "./json/frameworkTemplate/frameworkWithReferencedReports.json"
    private val currencyDataPoint = "./json/frameworkTemplate/currencyDataPointWithExtendedDocumentReference.json"

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, objectMapper, logMessageBuilder, specificationClient, datasetDatapointRepository,
        )

    private val spyDataPointManager = spy(dataPointManager)

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val dataPointIdentifier = "test-identifier"
    private val reportingPeriod = "test-period"
    private val companyId = "test-company-id"

    @BeforeEach
    fun resetMocks() {
        reset(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, logMessageBuilder, specificationClient, datasetDatapointRepository,
        )
    }

    @Test
    fun `check that the storeDataPoint function executes the expected calls and returns the expected results`() {
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointIdentifier = dataPointIdentifier,
                dataPointContent = "test-content",
                companyId = IdUtils.generateUUID(),
                reportingPeriod = reportingPeriod,
            )
        val expectedString = objectMapper.writeValueAsString(uploadedDataPoint)

        `when`(metaDataManager.storeDataPointMetaInformation(any())).thenAnswer { invocation ->
            val argument = invocation.getArgument<DataPointMetaInformationEntity>(0)
            DataPointMetaInformationEntity(
                dataId = argument.dataId,
                dataPointIdentifier = argument.dataPointIdentifier,
                uploaderUserId = argument.uploaderUserId,
                companyId = argument.companyId,
                reportingPeriod = argument.reportingPeriod,
                uploadTime = argument.uploadTime,
                currentlyActive = argument.currentlyActive,
                qaStatus = argument.qaStatus,
            )
        }

        val dataId = IdUtils.generateUUID()
        val result = dataPointManager.storeDataPoint(uploadedDataPoint, dataId, uploaderUserId, correlationId)

        verify(metaDataManager).storeDataPointMetaInformation(any())
        verify(dataManager).storeDataInTemporaryStorage(eq(dataId), eq(expectedString), eq(correlationId))
        assert(result.companyId == uploadedDataPoint.companyId)
        assert(result.dataPointIdentifier == uploadedDataPoint.dataPointIdentifier)
        assert(result.reportingPeriod == uploadedDataPoint.reportingPeriod)
        assert(result.dataId == dataId)
    }

    @Test
    fun `check that the new data id is set to active and the previous one set to inactive`() {
        val newActiveDataId = "test-new-active-data-id"
        val someId = "dummy"
        val differentIdDataPoint =
            BasicDataPointDimensions(
                companyId = "different-id",
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
            )
        `when`(metaDataManager.getCurrentlyActiveDataId(differentIdDataPoint)).thenReturn(someId)

        dataPointManager.updateCurrentlyActiveDataPoint(differentIdDataPoint, newActiveDataId, correlationId)
        verify(metaDataManager, times(1)).updateCurrentlyActiveFlagOfDataPoint(someId, null)
        verify(metaDataManager, times(1)).updateCurrentlyActiveFlagOfDataPoint(newActiveDataId, true)
    }

    @Test
    fun `check that no update happens to the active data id if it does not change or the new id is null`() {
        val newActiveDataId = "test-new-active-data-id"
        val returnNewActiveId =
            BasicDataPointDimensions(
                companyId = "same-id",
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
            )
        val returnNull =
            BasicDataPointDimensions(
                companyId = "no-id",
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
            )
        `when`(metaDataManager.getCurrentlyActiveDataId(returnNewActiveId)).thenReturn(newActiveDataId)
        `when`(metaDataManager.getCurrentlyActiveDataId(returnNull)).thenReturn(null)

        dataPointManager.updateCurrentlyActiveDataPoint(returnNewActiveId, newActiveDataId, correlationId)
        verify(metaDataManager, times(0)).updateCurrentlyActiveFlagOfDataPoint(any(), any())

        dataPointManager.updateCurrentlyActiveDataPoint(returnNull, null, correlationId)
        verify(metaDataManager, times(0)).updateCurrentlyActiveFlagOfDataPoint(any(), any())
    }

    @Test
    fun `check that processing a dataset works as expected`() {
        val expectedDataPointIdentifiers = listOf("extendedEnumFiscalYearDeviation", "extendedDateFiscalYearEnd", "extendedCurrencyEquity")
        val frameworkSpecification = TestResourceFileReader.getKotlinObject<FrameworkSpecificationDto>(frameworkSpecification)
        val inputData = TestResourceFileReader.getJsonString(inputData)

        `when`(specificationClient.getFrameworkSpecification(any())).thenReturn(frameworkSpecification)

        val uploadedDataSet =
            StorableDataSet(
                companyId = companyId,
                dataType = DataType("sfdr"),
                uploaderUserId = uploaderUserId,
                uploadTime = Instant.now().toEpochMilli(),
                reportingPeriod = reportingPeriod,
                data = inputData,
            )

        spyDataPointManager.processDataSet(uploadedDataSet, false, correlationId)
        expectedDataPointIdentifiers.forEach {
            verify(spyDataPointManager, times(1)).storeDataPoint(
                argThat { dataPointIdentifier == it }, any(), any(), any(),
            )
        }
        verify(messageQueuePublications, times(expectedDataPointIdentifiers.size)).publishDataPointUploadedMessage(any(), any(), any())
        verify(messageQueuePublications, times(1)).publishDataSetQaRequiredMessage(any(), any(), any())
        verify(messageQueuePublications, times(0)).publishDataSetUploadedMessage(any(), any(), any())
        verify(datasetDatapointRepository, times(1)).save(
            argThat {
                dataPoints.keys.sorted() == expectedDataPointIdentifiers.sorted()
            },
        )
    }

    @Test
    fun `check that assembling a dataset works as expected`() {
        val datasetId = "test-dataset-id"
        val frameworkSpecification = TestResourceFileReader.getKotlinObject<FrameworkSpecificationDto>(frameworkSpecification)
        val dataPointMap =
            mapOf(
                "extendedEnumFiscalYearDeviation" to "test-data-point-1",
                "extendedCurrencyEquity" to "test-data-point-2",
            )

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
                dataPointContent = dataContentMap[dataId]!!,
                dataPointIdentifier = dataPointMap.filterValues { it == dataId }.keys.first(),
                companyId = companyId,
                reportingPeriod = reportingPeriod,
            )
        }

        val assembledDataSet = dataPointManager.getDataSetFromId(datasetId, "sfdr", correlationId)
        dataPointContent.forEach {
            assert(assembledDataSet.contains(it))
        }
        assert(!assembledDataSet.contains("\"referencedReports\":null}"))
    }
}
