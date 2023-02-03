package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.dataland.datalandbackendutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import java.rmi.ServerException
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Simple implementation of a data store using a postgres database
 * @param dataItemRepository
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param rabbitTemplate
 */
//@RabbitListener(queues = ["storage_queue"])
@Component
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var nonPersistedDataClient: NonPersistedDataControllerApi,
    @Autowired var objectMapper: ObjectMapper,
    val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    /**
     * Method to insert data into a database
     * @param message Message retrieved from storage_queue
     */

    @RabbitListener(queues = ["storage_queue"])
    @RabbitHandler
    //TODO remove rabbithandler
    fun insertDataSet(message : Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        logger.info("Received DataID $dataId and CorrelationId: $correlationId")
        val data = nonPersistedDataClient.getCompanyAssociatedDataForInternalStorage(dataId)
        logger.info("Received DataID $dataId and DataDataDataStoreStoreStore: $data")
        logger.info("Inserting data into database with dataId: $dataId and correlation id: $correlationId.")
        try {
            dataItemRepository.save(DataItem(dataId, objectMapper.writeValueAsString(data)))
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(dataId, "Data successfully stored", correlationId ,"stored_queue")
        } catch (e: ServerException) {
            val internalMessage = "Error storing data." +
                    " Received ServerException with Message: ${e.message}. Correlation ID: $correlationId"
            logger.error(internalMessage)
            //TODO check that the error messages are applicable
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
