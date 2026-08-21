package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.model.StorableDataPoint
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class DataPointManagerTest {
    private val dataManager = mock(DataManager::class.java)
    private val metaDataManager = mock(DataPointMetaInformationManager::class.java)
    private val storageClient = mock(StorageControllerApi::class.java)
    private val messageQueuePublications = mock(MessageQueuePublications::class.java)
    private val dataPointValidator = mock(DataPointValidator::class.java)
    private val companyQueryManager = mock(CompanyQueryManager::class.java)
    private val companyRoleChecker = mock(CompanyRoleChecker::class.java)
    private val logMessageBuilder = mock(LogMessageBuilder::class.java)

    private val referencedReportsUtilities = ReferencedReportsUtilities()
    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, referencedReportsUtilities, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, defaultObjectMapper, logMessageBuilder,
        )

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val dataPointType = "test-type"
    private val reportingPeriod = "test-period"
    private val testDataProvider = TestDataProvider(defaultObjectMapper)

    @BeforeEach
    fun resetMocks() {
        reset(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, logMessageBuilder,
        )
    }

    @Test
    fun `check that the storeDataPoint function executes the expected calls and returns the expected results`() {
        val rawDataPointContent =
            TestResourceFileReader.getJsonString("json/dataPoints/numericDataPointHalf.json")
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointType = dataPointType,
                dataPoint = rawDataPointContent,
                companyId = IdUtils.generateUUID(),
                reportingPeriod = reportingPeriod,
            )
        val strippedDataPointContent =
            TestResourceFileReader.getJsonString("json/dataPoints/numericDataPointHalfDataSourceStripped.json")
        val expectedString =
            defaultObjectMapper.writeValueAsString(uploadedDataPoint.copy(dataPoint = strippedDataPointContent))

        `when`(metaDataManager.storeDataPointMetaInformation(any())).thenAnswer { invocation ->
            val argument = invocation.getArgument<DataPointMetaInformationEntity>(0)
            DataPointMetaInformationEntity(
                dataPointId = argument.dataPointId,
                dataPointType = argument.dataPointType,
                uploaderUserId = argument.uploaderUserId,
                companyId = argument.companyId,
                reportingPeriod = argument.reportingPeriod,
                uploadTime = argument.uploadTime,
                currentlyActive = argument.currentlyActive,
                qaStatus = argument.qaStatus,
            )
        }

        val dataId = IdUtils.generateUUID()
        val result = dataPointManager.storeDataPoint(uploadedDataPoint, dataId, uploaderUserId, 0, correlationId)

        verify(metaDataManager).storeDataPointMetaInformation(any())
        verify(dataManager).storeDataInTemporaryStorage(eq(dataId), eq(expectedString), eq(correlationId))
        assert(result.companyId == uploadedDataPoint.companyId)
        assert(result.dataPointType == uploadedDataPoint.dataPointType)
        assert(result.reportingPeriod == uploadedDataPoint.reportingPeriod)
        assert(result.dataPointId == dataId)
    }

    @Test
    fun `check that the processDataPoint function strips fileName and publicationDate before storing`() {
        val dummyDataSource =
            ExtendedDocumentReference(
                fileReference = "50a36c418baffd520bb92d84664f06f9732a21f4e2e5ecee6d9136f16e7e0b63",
                fileName = "AnnualReport",
                page = "6",
                tagName = "content",
                publicationDate = LocalDate.parse("2025-09-05"),
            )
        val castedDataPoint =
            ExtendedCurrencyDataPoint(
                value = BigDecimal("0.5"),
                currency = "USD",
                dataSource = dummyDataSource,
            )
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointType = dataPointType,
                dataPoint = "{\"value\": \"0.5\", \"currency\": \"USD\"}",
                companyId = IdUtils.generateUUID(),
                reportingPeriod = reportingPeriod,
            )

        `when`(dataPointValidator.validateDataPoint(any(), any(), any())).thenReturn(castedDataPoint)
        `when`(companyRoleChecker.canUserBypassQa(any())).thenReturn(true)
        doReturn(testDataProvider.getEmptyStoredCompanyEntity()).whenever(companyQueryManager).getCompanyById(any())

        dataPointManager.processDataPoint(uploadedDataPoint, uploaderUserId, true, correlationId)

        val storedDataPointCaptor = argumentCaptor<String>()
        verify(dataManager).storeDataInTemporaryStorage(any(), storedDataPointCaptor.capture(), eq(correlationId))

        val storedUploadedDataPoint =
            defaultObjectMapper.readValue(storedDataPointCaptor.firstValue, UploadedDataPoint::class.java)
        val storedDataSourceNode =
            defaultObjectMapper.readTree(storedUploadedDataPoint.dataPoint).get("dataSource")
        val storedDataSource =
            defaultObjectMapper.readValue(storedDataSourceNode.toString(), ExtendedDocumentReference::class.java)

        assertEquals(null, storedDataSource.fileName)
        assertEquals(null, storedDataSource.publicationDate)
        assertEquals(dummyDataSource.fileReference, storedDataSource.fileReference)
        assertEquals(dummyDataSource.page, storedDataSource.page)
        assertEquals(dummyDataSource.tagName, storedDataSource.tagName)
    }

    @Test
    fun `test that a datapoint is cast to the correct class on retrieval`() {
        val dummyDataPoint = "{\"value\": \"0.5\", \"currency\": \"USD\"}"
        val dummyDataPointType = "extendedCurrencyTotalAmountOfReportedFinesOfBriberyAndCorruption"
        val dummyDataPointId = IdUtils.generateUUID()
        val dummyCompanyId = "dummy company"
        val dummyReportingPeriod = "2005"
        val dummyCorrelationId = "test-correlation-id"

        doReturn(
            listOf(
                DataPointMetaInformationEntity(
                    dataPointId = dummyDataPointId,
                    dataPointType = dummyDataPointType,
                    uploaderUserId = "uploaderUserId",
                    companyId = dummyCompanyId,
                    reportingPeriod = dummyReportingPeriod,
                    uploadTime = 0,
                    currentlyActive = true,
                    qaStatus = QaStatus.Accepted,
                ),
            ),
        ).whenever(metaDataManager).getDataPointMetaInformationByIds(listOf(dummyDataPointId))

        doReturn(
            mapOf(
                dummyDataPointId to StorableDataPoint(dummyDataPoint, dummyDataPointType, dummyCompanyId, dummyReportingPeriod),
            ),
        ).whenever(storageClient).selectBatchDataPointsByIds(dummyCorrelationId, listOf(dummyDataPointId))

        doReturn(
            ExtendedCurrencyDataPoint(
                value = BigDecimal("0.5"),
                currency = "USD",
            ),
        ).whenever(dataPointValidator).validateDataPoint(dummyDataPointType, dummyDataPoint, dummyCorrelationId)

        val result = dataPointManager.retrieveDataPoint(dummyDataPointId, dummyCorrelationId)
        val expectation =
            defaultObjectMapper.writeValueAsString(
                defaultObjectMapper.readValue(dummyDataPoint, ExtendedCurrencyDataPoint::class.java),
            )
        Assertions.assertEquals(expectation, result.dataPoint)
    }
}
