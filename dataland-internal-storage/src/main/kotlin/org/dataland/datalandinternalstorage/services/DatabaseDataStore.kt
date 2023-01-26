package org.dataland.datalandinternalstorage.services

import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.ComponentScan

/**
 * Simple implementation of a data store using a postgres database
 */
@ComponentScan(basePackages = ["org.dataland"])
@Component
//@RabbitListener(queues = ["storage_queue"])
class DatabaseDataStore(
    @Autowired private var dataItemRepository: DataItemRepository,
    @Autowired var dataInformationHashMap : StorageHashMap,
    private val rabbitTemplate: RabbitTemplate,
) {

    /**
     * Insterts data into a database
     * @param data a json object
     * @return id associated with the stored data
     */
   // @RabbitHandler
    fun insertDataSet(correlationId: String) {
       // println("InsertDataSet: ${message.messageProperties.headers}")
        //println(dataId)
      // val testMessage = rabbitTemplate.receive("storage_queue")
        //if (testMessage?.messageProperties?.headers  != null){
         //   println("message exists")}
       // println(rabbitTemplate.receive("storage_queue"))
       // println("Stooooooooooooooooooooooooooorage")

        //val data = dataInformationHashMap.map[dataId]
       // println("Data to save: $data")
      //  dataItemRepository.save(DataItem(dataId, data!!))
      //  cloudEventBuilder.buildCEMessageAndSendToQueue(input = dataId, type = "DataId on Upload", queue = "stored_queue")
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
