package org.dataland.documentmanager.controller

import org.dataland.documentmanager.api.TemporarilyCachedDataApi
import org.dataland.documentmanager.services.InMemoryDocumentStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Implementation of the controller for delivering and removing temporarily stored data
 * @param inMemoryStore service to manage datasets from the in memory store
 */
@RestController
class TemporarilyCachedDataController(
    @Autowired private val inMemoryStore: InMemoryDocumentStore,
) : TemporarilyCachedDataApi {
    override fun getReceivedData(sha256hash: String): ResponseEntity<ByteArray> {
        val dataset = inMemoryStore.retrieveDataFromMemoryStore(sha256hash)
        return ResponseEntity.ok(dataset)
    }
}
