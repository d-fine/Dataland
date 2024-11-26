package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class DataPointManagerTest {
    private val dataManager = mock(DataManager::class.java)
    private val metaDataManager = mock(DataMetaInformationManager::class.java)
    private val storageClient = mock(StorageControllerApi::class.java)
    private val messageQueueInteractionForDataPoints = mock(MessageQueueInteractionForDataPoints::class.java)
    private val dataPointValidator = mock(DataPointValidator::class.java)
    private val objectMapper = mock(ObjectMapper::class.java)

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, storageClient, messageQueueInteractionForDataPoints, dataPointValidator, objectMapper,
        )

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"
    private val bypassQa = false

    @Test
    fun `check that the storeDataPoint function executes the expected calls and returns the expected results`() {
        val uploadedDataPoint =
            UploadedDataPoint(
                dataPointIdentifier = "test-identifier",
                dataPointContent = "test-content",
                companyId = IdUtils.generateUUID(),
                reportingPeriod = "test-period",
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

        val result = dataPointManager.storeDataPoint(uploadedDataPoint, uploaderUserId, bypassQa, correlationId)

        verify(metaDataManager).storeDataPointMetaInformation(any())
        verify(dataManager).storeDataInTemporaryStorage(any(), eq("json-content"), eq(correlationId))
        verify(messageQueueInteractionForDataPoints).publishDataPointUploadedMessage(any(), eq(correlationId))
        assert(result.companyId == uploadedDataPoint.companyId)
        assert(result.dataPointIdentifier == uploadedDataPoint.dataPointIdentifier)
        assert(result.reportingPeriod == uploadedDataPoint.reportingPeriod)
    }
}
