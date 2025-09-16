package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.model.StorableDataPoint
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class DataPointManagerTest {
    private val dataManager = mock(DataManager::class.java)
    private val metaDataManager = mock(DataPointMetaInformationManager::class.java)
    private val storageClient = mock(StorageControllerApi::class.java)
    private val messageQueuePublications = mock(MessageQueuePublications::class.java)
    private val dataPointValidator = mock(DataPointValidator::class.java)
    private val companyQueryManager = mock(CompanyQueryManager::class.java)
    private val companyRoleChecker = mock(CompanyRoleChecker::class.java)
    private val logMessageBuilder = mock(LogMessageBuilder::class.java)

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, defaultObjectMapper, logMessageBuilder,
        )

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val dataPointType = "test-type"
    private val reportingPeriod = "test-period"

    @BeforeEach
    fun resetMocks() {
        reset(
            dataManager, metaDataManager, storageClient, messageQueuePublications, dataPointValidator,
            companyQueryManager, companyRoleChecker, logMessageBuilder,
        )
    }

    @Test
    fun `check that the storeDataPoint function executes the expected calls and returns the expected results`() {
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointType = dataPointType,
                dataPoint = "test-content",
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
    fun `test that a datapoint is casted to the correct class on retrieval`() {
        val dummyDataPoint = "{\"value\": \"0.5\", \"currency\": \"USD\"}"
        val dummyDataPointType = "extendedCurrencyTotalAmountOfReportedFinesOfBriberyAndCorruption"
        val dummyDataPointId = IdUtils.generateUUID()
        val dummyCompanyId = "dummy dompany"
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
