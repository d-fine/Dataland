package org.dataland.datalandinternalstorage.controller

import org.dataland.datalandinternalstorage.api.StorageAPI
import org.dataland.datalandinternalstorage.services.DatabaseBlobDataStore
import org.dataland.datalandinternalstorage.services.DatabaseStringDataStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream

/**
 * Implementation of Storage Controller
 * @param stringDataStore a database store for strings
 * @param blobDataStore a database store for blobs
 */
@RestController
@Component("StorageController")
class StorageController(
    @Autowired val stringDataStore: DatabaseStringDataStore,
    @Autowired val blobDataStore: DatabaseBlobDataStore,
) : StorageAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun selectDataById(dataId: String, correlationId: String): ResponseEntity<String> {
        logger.info("Selecting data from database with data id: $dataId. Correlation id: $correlationId.")
        return ResponseEntity.ok(stringDataStore.selectDataSet(dataId, correlationId))
    }

    override fun selectBlobById(blobId: String, correlationId: String): ResponseEntity<InputStreamResource> {
        logger.info("Selecting blob from database with hash: $blobId. Correlation id: $correlationId.")
        val blob = blobDataStore.selectBlobById(blobId, correlationId)
        val stream = ByteArrayInputStream(blob)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(stream))
    }
}
