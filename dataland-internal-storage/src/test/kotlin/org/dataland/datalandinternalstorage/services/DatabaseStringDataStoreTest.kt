package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.data.DataIdPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataMetaInfoPatchMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadedPayload
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.internal.verification.VerificationModeFactory.times
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
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    private val dataId = UUID.randomUUID().toString()
    private val correlationId = UUID.randomUUID().toString()

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
        `when`(mockDataItemRepository.findById(dataId)).thenReturn(Optional.empty())
        assertThrows<ResourceNotFoundApiException> { databaseStringDataStore.selectDataSet(dataId, correlationId) }
    }

    @Test
    fun `check that storeUploadedDatasets works on upload event`() {
        val dummyData = "{some dummy data}"
        val dataUploadedPayload =
            objectMapper.writeValueAsString(DataUploadedPayload(dataId = dataId, bypassQa = false))
        val routingKey = RoutingKeyNames.DATASET_UPLOAD

        `when`(mockTemporarilyCachedDataControllerApi.getReceivedPublicData(dataId)).thenReturn(dummyData)

        assertDoesNotThrow {
            databaseStringDataStore.storeDataset(
                dataUploadedPayload,
                correlationId,
                MessageType.PUBLIC_DATA_RECEIVED,
                routingKey,
            )
        }
        verify(mockTemporarilyCachedDataControllerApi, times(1)).getReceivedPublicData(dataId)
        verify(mockDataItemRepository, times(1)).save(any())
        verify(mockCloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(DataIdPayload(dataId)),
            MessageType.DATA_STORED,
            correlationId,
            ExchangeName.ITEM_STORED,
            RoutingKeyNames.DATA,
        )
    }

    @Test
    fun `check that storeUploadedDatasets works on patch event`() {
        val uploaderUserId = "uploader1234"
        val dummyData = "{some dummy data}"
        val dataPatchPayload =
            objectMapper.writeValueAsString(DataMetaInfoPatchMessage(dataId, uploaderUserId))
        val routingKey = RoutingKeyNames.METAINFORMATION_PATCH

        `when`(mockTemporarilyCachedDataControllerApi.getReceivedPublicData(dataId)).thenReturn(dummyData)

        assertDoesNotThrow {
            databaseStringDataStore.storeDataset(
                dataPatchPayload,
                correlationId,
                MessageType.METAINFO_UPDATED,
                routingKey,
            )
        }
        verify(mockTemporarilyCachedDataControllerApi, times(1)).getReceivedPublicData(dataId)
        verify(mockDataItemRepository, times(1)).save(any())
        verify(mockCloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
            objectMapper.writeValueAsString(DataIdPayload(dataId)),
            MessageType.DATA_STORED,
            correlationId,
            ExchangeName.ITEM_STORED,
            RoutingKeyNames.DATA,
        )
    }

    @Test
    fun `check that storeUploadedDatasets throws an error on unknown routing key`() {
        val unknownRoutingKey = "someWeirdRoutingKey"

        val exceptionThrown =
            assertThrows<MessageQueueRejectException> {
                databaseStringDataStore.storeDataset(
                    "",
                    correlationId,
                    MessageType.METAINFO_UPDATED,
                    unknownRoutingKey,
                )
            }
        Assertions.assertEquals(
            exceptionThrown.message,
            "Routing Key $unknownRoutingKey unknown. " +
                "Expected Routing Key ${RoutingKeyNames.DATASET_UPLOAD} or ${RoutingKeyNames.METAINFORMATION_PATCH}",
        )
        verify(mockTemporarilyCachedDataControllerApi, times(0)).getReceivedPublicData(dataId)
    }

    @Test
    fun `check that storeUploadedDatasets throws an error on unknown message type`() {
        val unknownMessageType = "unknownMessageType"

        assertThrows<MessageQueueRejectException> {
            databaseStringDataStore.storeDataset(
                "",
                correlationId,
                unknownMessageType,
                RoutingKeyNames.DATASET_UPLOAD,
            )
        }
        verify(mockTemporarilyCachedDataControllerApi, times(0)).getReceivedPublicData(dataId)
    }
}
