package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.services.datapoints.MessageQueueInteractionForDataPoints
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.model.DataPointDimensions
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class DataPointManagerTest {
    private val dataManager = mock(DataManager::class.java)
    private val metaDataManager = mock(DataPointMetaInformationManager::class.java)
    private val storageClient = mock(StorageControllerApi::class.java)
    private val messageQueueInteractionForDataPoints = mock(MessageQueueInteractionForDataPoints::class.java)
    private val dataPointValidator = mock(DataPointValidator::class.java)
    private val objectMapper = mock(ObjectMapper::class.java)
    private val companyQueryManager = mock(CompanyQueryManager::class.java)
    private val companyRoleChecker = mock(CompanyRoleChecker::class.java)
    private val logMessageBuilder = mock(LogMessageBuilder::class.java)

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, storageClient, messageQueueInteractionForDataPoints, dataPointValidator,
            companyQueryManager, companyRoleChecker, objectMapper, logMessageBuilder,
        )

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val dataPointIdentifier = "test-identifier"
    private val reportingPeriod = "test-period"

    @Test
    fun `check that the storeDataPoint function executes the expected calls and returns the expected results`() {
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointIdentifier = dataPointIdentifier,
                dataPointContent = "test-content",
                companyId = IdUtils.generateUUID(),
                reportingPeriod = reportingPeriod,
            )

        `when`(objectMapper.writeValueAsString(uploadedDataPoint)).thenReturn("json-content")

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
        verify(dataManager).storeDataInTemporaryStorage(eq(dataId), eq("json-content"), eq(correlationId))
        assert(result.companyId == uploadedDataPoint.companyId)
        assert(result.dataPointIdentifier == uploadedDataPoint.dataPointIdentifier)
        assert(result.reportingPeriod == uploadedDataPoint.reportingPeriod)
        assert(result.dataId == dataId)
    }

    @Test
    fun `check that the new data id is set to active and the previous one set to inactive`() {
        val newActiveDataId = "test-new-active-data-id"
        val someId = "dummy"
        val returnDifferentId =
            DataPointDimensions(
                companyId = "different-id",
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
            )
        `when`(metaDataManager.getCurrentlyActiveDataId(returnDifferentId)).thenReturn(someId)

        dataPointManager.updateCurrentlyActiveDataPoint(returnDifferentId, newActiveDataId, correlationId)
        verify(metaDataManager, times(1)).updateCurrentlyActiveFlagOfDataPoint(someId, false)
        verify(metaDataManager, times(1)).updateCurrentlyActiveFlagOfDataPoint(newActiveDataId, true)
    }

    @Test
    fun `check that no update happens to the active data id if it does not change or the new id is null`() {
        val newActiveDataId = "test-new-active-data-id"
        val returnNewActiveId =
            DataPointDimensions(
                companyId = "same-id",
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
            )
        val returnNull =
            DataPointDimensions(
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
}
