package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandinternalstorage.DatalandInternalStorage
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [DatalandInternalStorage::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class DatabaseDataStoreTest(
    @Autowired val nonPersistedDataClient: NonPersistedDataControllerApi,
    @Autowired val objectMapper: ObjectMapper
) {
    val mockDataItemRepository: DataItemRepository = mock(DataItemRepository::class.java)
    val mockCloudEventMessageHandler: CloudEventMessageHandler = mock(CloudEventMessageHandler::class.java)
    val databaseDataStore = DatabaseDataStore(mockDataItemRepository, mockCloudEventMessageHandler, nonPersistedDataClient, objectMapper)
    val dataId = "TestDataId"
    val data = "TestDataForTestDataId"
    val correlationId = "TestCorrelationId"

    @Test
    fun `check that a Server Exception is thrown when the data storage reports a Server Exception during insertion`() {
        `when`(mockDataItemRepository.save(DataItem(dataId, objectMapper.writeValueAsString(data)))).thenThrow(
            ServerException::class.java,
        )
        assertThrows<ServerException> {
            databaseDataStore.insertDataAndSendNotification(dataId, data, correlationId)
        }
    }
}
