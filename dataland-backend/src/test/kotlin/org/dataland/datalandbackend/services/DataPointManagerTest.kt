package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.mockito.Mockito.mock

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
}
