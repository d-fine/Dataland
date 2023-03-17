package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class DatabaseDataStoreTest {

    val mockDataItemRepository: DataItemRepository = mock(DataItemRepository::class.java)
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val mockTemporarilyCachedDataControllerApi: TemporarilyCachedDataControllerApi =
        mock(TemporarilyCachedDataControllerApi::class.java)
    lateinit var databaseDataStore: DatabaseDataStore
    lateinit var spyDatabaseDataStore: DatabaseDataStore
    val correlationId = UUID.randomUUID().toString()
    val objectMapper: ObjectMapper = ObjectMapper()
    val messageQueueUtils: MessageQueueUtils = MessageQueueUtils()

    @BeforeEach
    fun reset() {
        databaseDataStore = DatabaseDataStore(
            mockDataItemRepository,
            mockCloudEventMessageHandler,
            mockTemporarilyCachedDataControllerApi,
            objectMapper,
            messageQueueUtils,
        )
        spyDatabaseDataStore = spy(databaseDataStore)
    }

    @Test
    fun `check that a ResourceNotFoundApiException is thrown if the dataset could not be found`() {
        val dataId = "dummyId"
        `when`(mockDataItemRepository.findById(dataId)).thenReturn(Optional.empty())
        assertThrows<ResourceNotFoundApiException> { databaseDataStore.selectDataSet(dataId, correlationId) }
    }
}
