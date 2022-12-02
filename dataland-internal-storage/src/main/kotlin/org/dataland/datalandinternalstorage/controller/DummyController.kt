package org.dataland.datalandinternalstorage.controller

import org.dataland.datalandinternalstorage.api.StorageAPI
import org.dataland.datalandinternalstorage.service.DatabaseDataStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of API
 */
@RestController
class DummyController(
    @Autowired val dataStore: DatabaseDataStore
) : StorageAPI {

    override fun selectDataById(dataId: String, correlationId: String?): ResponseEntity<String> {
        return ResponseEntity.ok(dataStore.selectDataSet(dataId))
    }

    override fun insertData(correlationId: String?, body: String?): ResponseEntity<String> {
        return ResponseEntity.ok(dataStore.insertDataSet(body ?: ""))
    }

//    override fun checkHealth(): ResponseEntity<CheckHealthResponse> {
//        return ResponseEntity.ok(CheckHealthResponse("I am alive!"))
//    }
}
