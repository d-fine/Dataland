package org.dataland.datalandinternalstorage.services

import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.UUID
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.ComponentScan
import org.dataland.datalandbackendutils.cloudevents.CloudEventMessageHandler

/**
 * Simple implementation of a data store using a postgres database
 */
@ComponentScan(basePackages = ["org.dataland"])
@Component
@RabbitListener(queues = ["storage_queue"])
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository,
    private val rabbitTemplate: RabbitTemplate,
    @Autowired var dataInformationHashMap : StorageHashMap,
    @Autowired var cloudEventBuilder: CloudEventMessageHandler,
) {

    /**
     * Insterts data into a database
     * @param data a json object
     * @return id associated with the stored data
     */
   // @RabbitListener(queues = ["storage_queue"])
    @RabbitHandler
    fun insertDataSet(correlationId :String) {
       // val correlationId = rabbitTemplate.receive("storage_queue")
        println(correlationId)
        println("Stooooooooooooooooooooooooooorage")
        val dataId = "${UUID.randomUUID()}:${UUID.randomUUID()}_${UUID.randomUUID()}"
        val data = dataInformationHashMap.map[correlationId]
        println("Data to save: $data")
        dataItemRepository.save(DataItem(dataId, data!!))
        cloudEventBuilder.buildCEMessageAndSendToQueue(input = dataId, type = "DataId on Upload", queue = "stored_queue")
        //return dataId
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
