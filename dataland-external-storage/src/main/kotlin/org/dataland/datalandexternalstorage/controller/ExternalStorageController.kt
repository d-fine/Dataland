package org.dataland.datalandexternalstorage.controller

import org.dataland.datalandexternalstorage.api.ExternalStorageAPI
import org.slf4j.LoggerFactory
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
class ExternalStorageController(
) : ExternalStorageAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun selectDataById(dataId: String, correlationId: String): ResponseEntity<String> {
        logger.info("Selecting data from database with data ID: $dataId. Correlation ID: $correlationId.")
        return ResponseEntity.ok("stringDataStore.selectDataSet(dataId, correlationId)")
    }

    override fun selectBlobById(blobId: String, correlationId: String): ResponseEntity<InputStreamResource> {
        logger.info("Selecting blob from database with hash: $blobId. Correlation id: $correlationId.")
        //val blob = blobDataStore.selectBlobById(blobId, correlationId)
        //TODO korrekte logik einbauen
        val byte = byteArrayOf()
        val stream = ByteArrayInputStream(byte)
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(stream))
    }
}
