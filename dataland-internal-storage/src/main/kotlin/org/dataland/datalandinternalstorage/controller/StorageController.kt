package org.dataland.datalandinternalstorage.controller

import org.dataland.datalandinternalstorage.api.StorageAPI
import org.dataland.datalandinternalstorage.models.InsertDataResponse
import org.dataland.datalandinternalstorage.services.DatabaseDataStore
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitHandler
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of Storage Controller
 * @param dataStore a database data store
 */
@RestController
@ComponentScan(basePackages = ["org.dataland"])
@Component("StorageController")
//@RabbitListener(queues = ["storage_queue"])
class StorageController(
    @Autowired val dataStore: DatabaseDataStore,
    private val rabbitTemplate: RabbitTemplate,
) : StorageAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun selectDataById(dataId: String, correlationId: String): ResponseEntity<String> {
        logger.info("Selecting data from database with (data id: $dataId) and (correlation id: $correlationId).")
        return ResponseEntity.ok(dataStore.selectDataSet(dataId))
    }
    //@RabbitListener(queues = ["storage_queue"])
    //@RabbitHandler
    override fun insertData(correlationId :String): ResponseEntity<InsertDataResponse> {
        //val correlationId2 = rabbitTemplate.receiveAndConvert("storage_queue")
        //println(correlationId2)
       // val correlationId = correlationId2.toString()
        print("StorageController")
        //println(correlationId)
        logger.info("Inserting data into database with (correlation id: $correlationId).")
        return ResponseEntity.ok(InsertDataResponse(dataStore.insertDataSet(correlationId).toString()))
    }
}
