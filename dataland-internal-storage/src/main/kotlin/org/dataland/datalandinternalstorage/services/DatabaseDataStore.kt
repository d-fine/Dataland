package org.dataland.datalandinternalstorage.services

import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.ComponentScan

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
        println("Data to save: $data")
        dataItemRepository.save(DataItem(dataId, data!!))
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(dataId, "DataId on Upload", correlationId ,"stored_queue")
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
