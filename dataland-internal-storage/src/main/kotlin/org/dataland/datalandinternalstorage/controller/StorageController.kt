package org.dataland.datalandinternalstorage.controller

import org.dataland.datalandinternalstorage.api.StorageAPI
import org.dataland.datalandinternalstorage.models.InsertDataResponse
import org.dataland.datalandinternalstorage.services.DatabaseDataStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of Storage Controller
 * @param dataStore a database data store
 */
@RestController
@Component("StorageController")
class StorageController(
    @Autowired val dataStore: DatabaseDataStore
) : StorageAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun selectDataById(dataId: String, correlationId: String): ResponseEntity<String> {
        logger.info("Selecting data with (data id: $dataId) and (correlation id: $correlationId).")
        return ResponseEntity.ok(dataStore.selectDataSet(dataId))
    }

    override fun insertData(correlationId: String, body: String): ResponseEntity<InsertDataResponse> {
        logger.info("Inserting data with (correlation id: $correlationId).")
        return ResponseEntity.ok(InsertDataResponse(dataStore.insertDataSet(body)))
    }
}
