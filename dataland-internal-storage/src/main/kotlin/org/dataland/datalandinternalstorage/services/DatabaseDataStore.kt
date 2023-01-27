package org.dataland.datalandinternalstorage.services

import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.models.StorageHashMap
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.ComponentScan
import java.rmi.ServerException

/**
 * Simple implementation of a data store using a postgres database
 */
@ComponentScan(basePackages = ["org.dataland"])
@Component
@RabbitListener(queues = ["storage_queue"])
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository,
    @Autowired var dataInformationHashMap : StorageHashMap,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    /**
     * Insterts data into a database
     * @param message a message object retrieved from the message queue
     * @return id associated with the stored data
     */
   // @RabbitHandler
    fun insertDataSet(message : Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.messageId
        val data = dataInformationHashMap.map[dataId]
        logger.info("Inserting data into database with dataId: $dataId and correlation id: $correlationId.")
        try {dataItemRepository.save(DataItem(dataId, data!!))
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(dataId, "Data sucessfully stored", correlationId ,"stored_queue")
        } catch (e: ServerException) {
            val internalMessage = "Error storing data." +
                    " Received ServerException with Message: ${e.message}. Correlation ID: $correlationId"
            logger.error(internalMessage)
            throw InternalServerErrorApiException(
                "Upload to Storage failed", "The upload of the dataset to the Storage failed",
                internalMessage,
                e
            )
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
