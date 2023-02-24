package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.FanoutExchange
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Simple implementation of a data store using a postgres database
 * @param dataItemRepository
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param nonPersistedDataClient the service for retrieving data from the temporary storage
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */
@Component
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var nonPersistedDataClient: NonPersistedDataControllerApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired private var fanoutInternalStorage: FanoutExchange,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to the storage_queue and stores data into the database in case there is a message on the
     * storage_queue
     * @param message Message retrieved from storage_queue
     */

    //@RabbitListener(queues = ["storage_queue"])
    @RabbitListener(bindings = [QueueBinding(value = Queue("foo"),
        exchange = Exchange("dataReceived"),
        key = [""])])
    fun listenToStorageQueueAndTransferDataFromTemporaryToPersistentStorage(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        logger.info("Received DataID $dataId and CorrelationId: $correlationId")
        val data = nonPersistedDataClient.getCompanyAssociatedDataForInternalStorage(dataId)
        logger.info("Received DataID $dataId and DataDataDataStoreStoreStore: $data")
        logger.info("Inserting data into database with dataId: $dataId and correlation id: $correlationId.")
        insertDataAndSendMessage(dataId, data, correlationId)
    }

    /**
     * Method to actually insert data into the database and send a message to stored_queue after it has finished
     * @param dataId to identify the data as first property of item to store
     * @param data the data to be stored in the storage
     * @param correlationId of the request initiating the storing of data
     */
    @RabbitHandler
    fun insertDataAndSendMessage(dataId: String, data: String, correlationId: String) {
        try {
            dataItemRepository.save(DataItem(dataId, objectMapper.writeValueAsString(data)))
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataId, "Data successfully stored", correlationId, fanoutInternalStorage.name,
            )
        } catch (exception: IllegalArgumentException) {
            val internalMessage = "Error storing data." +
                "Received IllegalArgumentException with message: ${exception.message}. Correlation ID: $correlationId."
            logger.error(internalMessage)
            throw IllegalArgumentException(internalMessage, exception)
        }
    }

    /**
     * Reads data from a database
     * @param dataId the id of the data to be retrieved
     * @return the data as json string with id dataId
     */
    fun selectDataSet(dataId: String): String {
        return dataItemRepository.findById(dataId).orElse(DataItem("", "")).data
    }
}
