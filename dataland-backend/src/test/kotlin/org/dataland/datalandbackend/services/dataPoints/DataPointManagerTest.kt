package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataDeliveryService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.TestResourceFileReader
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DataPointManagerTest {
    private val dataManager = mock(DataManager::class.java)
    private val metaDataManager = mock(DataPointMetaInformationManager::class.java)
    private val messageQueuePublications = mock(MessageQueuePublications::class.java)
    private val dataPointValidator = mock(DataPointValidator::class.java)
    private val companyQueryManager = mock(CompanyQueryManager::class.java)
    private val companyRoleChecker = mock(CompanyRoleChecker::class.java)
    private val logMessageBuilder = mock(LogMessageBuilder::class.java)
    private val dataDeliveryService = mock(DataDeliveryService::class.java)

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, defaultObjectMapper, logMessageBuilder, dataDeliveryService,
        )

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val dataPointType = "test-type"
    private val reportingPeriod = "test-period"

    @BeforeEach
    fun resetMocks() {
        reset(
            dataManager, metaDataManager, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, logMessageBuilder, dataDeliveryService,
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
        val expectedString = defaultObjectMapper.writeValueAsString(uploadedDataPoint)

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
    fun `test that a datapoint is returned without validation on retrieval`() {
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
                dummyDataPointId to
                    UploadedDataPoint(dummyDataPoint, dummyDataPointType, dummyCompanyId, dummyReportingPeriod),
            ),
        ).whenever(dataDeliveryService).assembleDatasetsFromDataPointIds(
            eq(listOf(dummyDataPointId)),
            any<Map<BasicDatasetDimensions, List<UploadedDataPoint>>>(),
            eq(dummyCorrelationId),
        )

        val result = dataPointManager.retrieveDataPoint(dummyDataPointId, dummyCorrelationId)

        Assertions.assertEquals(dummyDataPoint, result.dataPoint)
        verify(dataPointValidator, never()).validateDataPoint(any(), any(), any())
    }
}
