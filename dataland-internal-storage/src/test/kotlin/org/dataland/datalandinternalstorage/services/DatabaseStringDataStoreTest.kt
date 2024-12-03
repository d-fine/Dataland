package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID

class DatabaseStringDataStoreTest {
    private val mockDataItemRepository: DataItemRepository = mock(DataItemRepository::class.java)
    private val mockDataPointItemRepository: DataPointItemRepository = mock(DataPointItemRepository::class.java)
    private val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    private val mockTemporarilyCachedDataControllerApi: TemporarilyCachedDataControllerApi =
        mock(TemporarilyCachedDataControllerApi::class.java)
    private lateinit var databaseStringDataStore: DatabaseStringDataStore
    private lateinit var spyDatabaseStringDataStore: DatabaseStringDataStore
    private val correlationId = UUID.randomUUID().toString()
    private val objectMapper: ObjectMapper = ObjectMapper()

    @BeforeEach
    fun reset() {
        databaseStringDataStore =
            DatabaseStringDataStore(
                mockDataItemRepository,
                mockDataPointItemRepository,
                mockCloudEventMessageHandler,
                mockTemporarilyCachedDataControllerApi,
                objectMapper,
            )
        spyDatabaseStringDataStore = spy(databaseStringDataStore)
    }

    @Test
    fun `check that a ResourceNotFoundApiException is thrown if the dataset could not be found`() {
        val dataId = "dummyId"
        `when`(mockDataItemRepository.findById(dataId)).thenReturn(Optional.empty())
        assertThrows<ResourceNotFoundApiException> { databaseStringDataStore.selectDataSet(dataId, correlationId) }
    }
}
