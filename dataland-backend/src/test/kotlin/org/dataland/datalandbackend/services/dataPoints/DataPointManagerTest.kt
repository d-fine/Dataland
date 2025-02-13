package org.dataland.datalandbackend.services.dataPoints

import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.JsonTestUtils.testObjectMapper
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

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
            companyQueryManager, companyRoleChecker, testObjectMapper, logMessageBuilder,
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
        val expectedString = testObjectMapper.writeValueAsString(uploadedDataPoint)

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
    fun `check that the new data id is set to active and the previous one set to inactive`() {
        val newActiveDataId = "test-new-active-data-id"
        val someId = "dummy"
        val differentIdDataPoint =
            BasicDataPointDimensions(
                companyId = "different-id",
                dataPointType = dataPointType,
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
                dataPointType = dataPointType,
                reportingPeriod = reportingPeriod,
            )
        val returnNull =
            BasicDataPointDimensions(
                companyId = "no-id",
                dataPointType = dataPointType,
                reportingPeriod = reportingPeriod,
            )
        `when`(metaDataManager.getCurrentlyActiveDataId(returnNewActiveId)).thenReturn(newActiveDataId)
        `when`(metaDataManager.getCurrentlyActiveDataId(returnNull)).thenReturn(null)

        dataPointManager.updateCurrentlyActiveDataPoint(returnNewActiveId, newActiveDataId, correlationId)
        verify(metaDataManager, times(0)).updateCurrentlyActiveFlagOfDataPoint(any(), any())

        dataPointManager.updateCurrentlyActiveDataPoint(returnNull, null, correlationId)
        verify(metaDataManager, times(0)).updateCurrentlyActiveFlagOfDataPoint(any(), any())
    }
}
