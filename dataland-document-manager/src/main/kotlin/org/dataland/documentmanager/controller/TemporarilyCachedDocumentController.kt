package org.dataland.documentmanager.controller

import org.dataland.documentmanager.api.TemporarilyCachedDocumentApi
import org.dataland.documentmanager.services.InMemoryDocumentStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream

/**
 * Implementation of the controller for delivering and removing temporarily stored data
 * @param inMemoryStore service to manage datasets from the in memory store
 */
@RestController
class TemporarilyCachedDocumentController(
    @Autowired private val inMemoryStore: InMemoryDocumentStore,
) : TemporarilyCachedDocumentApi {
    override fun getReceivedData(sha256hash: String): ResponseEntity<InputStreamResource> {
        val blob = inMemoryStore.retrieveDataFromMemoryStore(sha256hash)
        val stream = ByteArrayInputStream(blob)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(stream))
    }
}
