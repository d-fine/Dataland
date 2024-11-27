package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataPointMetaInformationEntity
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationChanges
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.services.datapoints.MessageQueueInteractionForDataPoints
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
    private val metaDataManager = mock(DataPointMetaInformationManager::class.java)
    private val metaDataChanges = mock(DataPointMetaInformationChanges::class.java)
    private val storageClient = mock(StorageControllerApi::class.java)
    private val messageQueueInteractionForDataPoints = mock(MessageQueueInteractionForDataPoints::class.java)
    private val dataPointValidator = mock(DataPointValidator::class.java)
    private val objectMapper = mock(ObjectMapper::class.java)
    private val companyQueryManager = mock(CompanyQueryManager::class.java)
    private val companyRoleChecker = mock(CompanyRoleChecker::class.java)
    private val logMessageBuilder = mock(LogMessageBuilder::class.java)

    private val dataPointManager =
        DataPointManager(
            dataManager, metaDataManager, metaDataChanges, storageClient, messageQueueInteractionForDataPoints, dataPointValidator, companyQueryManager,
            companyRoleChecker, objectMapper, logMessageBuilder,
        )

    private val correlationId = "test-correlation-id"
    private val uploaderUserId = "test-user-id"

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

        `when`(metaDataChanges.storeDataPointMetaInformation(any())).thenAnswer { invocation ->
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

        verify(metaDataChanges).storeDataPointMetaInformation(any())
        verify(dataManager).storeDataInTemporaryStorage(eq(dataId), eq("json-content"), eq(correlationId))
        assert(result.companyId == uploadedDataPoint.companyId)
        assert(result.dataPointIdentifier == uploadedDataPoint.dataPointIdentifier)
        assert(result.reportingPeriod == uploadedDataPoint.reportingPeriod)
        assert(result.dataId == dataId)
    }
}
