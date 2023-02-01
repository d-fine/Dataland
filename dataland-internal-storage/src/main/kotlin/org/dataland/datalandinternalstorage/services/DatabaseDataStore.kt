package org.dataland.datalandinternalstorage.services

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.dataland.datalandbackend.openApiClient.api.NonPersistedDataControllerApi
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory
import java.rmi.ServerException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Simple implementation of a data store using a postgres database
 * @param dataItemRepository
 * @param dataInformationHashMap backend map for temporarily storing data information
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param rabbitTemplate
 */
@ComponentScan(basePackages = ["org.dataland"])
@Component
//@RabbitListener(queues = ["storage_queue"])
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var nonPersistedDataClient: NonPersistedDataControllerApi,
    val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    /**
     * Method to insert data into a database
     * @param message Message retrieved from storage_queue
     */
    @RabbitListener(queues = ["storage_queue"])
    fun insertDataSet(message : Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        println("Received DataId in DataBaseDataStore: $dataId")
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        println(correlationId)
        val data = retrieveDataViaApiCallToTemporaryStore(dataId)
        println("Received DataID $dataId")
        println("DataDataDataStoreStoreStore: $data")
        //val data = dataInformationHashMap.map[dataId]
        logger.info("Inserting data into database with dataId: $dataId and correlation id: $correlationId.")
        try {dataItemRepository.save(DataItem(dataId, data))
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(dataId, "Data successfully stored", correlationId ,"stored_queue")
        } catch (e: ServerException) {
            val internalMessage = "Error storing data." +
                    " Received ServerException with Message: ${e.message}. Correlation ID: $correlationId"
            logger.error(internalMessage)
            //TODO backend-utils must be included for this to work
            /*throw InternalServerErrorApiException(
                "Upload to Storage failed", "The upload of the dataset to the Storage failed",
                internalMessage,
                e
            )*/
        }
    }
    private fun retrieveDataViaApiCallToTemporaryStore(dataId: String): String{
        return nonPersistedDataClient.getCompanyAssociatedDataForInternalStorage(dataId)
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
