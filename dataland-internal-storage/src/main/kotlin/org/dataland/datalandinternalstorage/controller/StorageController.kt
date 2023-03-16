package org.dataland.datalandinternalstorage.controller

import org.dataland.datalandinternalstorage.api.StorageAPI
import org.dataland.datalandinternalstorage.services.DatabaseDataStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
    @Autowired val dataStore: DatabaseDataStore,
) : StorageAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun selectDataById(dataId: String, correlationId: String): ResponseEntity<String> {
        logger.info("Selecting data from database with data id: $dataId. Correlation id: $correlationId.")
        val data = dataStore.selectDataSet(dataId)
        if (data == null) {
            logger.info("Data with data id: $dataId could not be found. Correlation id: $correlationId.")
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }
        return ResponseEntity.ok(data)
    }
}
