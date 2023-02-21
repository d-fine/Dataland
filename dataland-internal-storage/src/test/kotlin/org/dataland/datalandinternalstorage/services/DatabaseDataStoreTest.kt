package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.dataland.datalandinternalstorage.DatalandInternalStorage
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.OptimisticLockingFailureException
import java.lang.IllegalArgumentException

@SpringBootTest(classes = [DatalandInternalStorage::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DatabaseDataStoreTest(
    @Autowired val objectMapper: ObjectMapper,
) {
    val mockDataItemRepository: DataItemRepository = mock(DataItemRepository::class.java)
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val mockNonPersistedDataClient: NonPersistedDataControllerApi = mock(NonPersistedDataControllerApi::class.java)
    val databaseDataStore = DatabaseDataStore(
        mockDataItemRepository, mockCloudEventMessageHandler,
        mockNonPersistedDataClient, objectMapper,
    )
    val dataId = "TestDataId"
    val data = "TestDataForTestDataId"
    val correlationId = "TestCorrelationId"

    @Test
    fun `check that an exception is thrown during insertion when the input is invalid`() {
        `when`(mockDataItemRepository.save(DataItem(dataId, objectMapper.writeValueAsString(data)))).thenThrow(
            IllegalArgumentException::class.java,
        )
        assertThrows<IllegalArgumentException> {
            databaseDataStore.insertDataAndSendNotification(dataId, data, correlationId)
        }
    }

    @Test
    fun `check that an exception is thrown during insertion when there is a version mismatch`() {
        `when`(mockDataItemRepository.save(DataItem(dataId, objectMapper.writeValueAsString(data)))).thenThrow(
            OptimisticLockingFailureException::class.java,
        )
        assertThrows<OptimisticLockingFailureException> {
            databaseDataStore.insertDataAndSendNotification(dataId, data, correlationId)
        }
    }

    @Test
    fun `check that an exception is thrown when sending a success notification to message queue fails`() {
        `when`(
            mockCloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataId, "Data successfully stored", correlationId, "stored_queue",
            ),
        ).thenThrow(
            AmqpException::class.java,
        )
        assertThrows<AmqpException> {
            databaseDataStore.insertDataAndSendNotification(dataId, data, correlationId)
        }
    }
}
