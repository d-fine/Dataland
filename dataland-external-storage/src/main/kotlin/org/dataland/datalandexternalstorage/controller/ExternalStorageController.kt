package org.dataland.datalandexternalstorage.controller

import org.dataland.datalandexternalstorage.api.ExternalStorageAPI
import org.dataland.datalandexternalstorage.services.EurodatDataStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream

/**
 * Implementation of external Storage Controller
 * @param eurodatDataStore manager to handle data requests to the external data storage
 */
@RestController
@Component("ExternalStorageController")
class ExternalStorageController(
    @Autowired private val eurodatDataStore: EurodatDataStore,
) : ExternalStorageAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun selectDataById(
        dataId: String,
        correlationId: String,
    ): ResponseEntity<String> {
        logger.info("Selecting data from EuroDaT storage with data ID: $dataId. Correlation ID: $correlationId.")
        return ResponseEntity.ok(eurodatDataStore.selectPrivateDataSet(dataId, correlationId))
    }

    override fun selectBlobById(
        blobId: String,
        correlationId: String,
    ): ResponseEntity<InputStreamResource> {
        logger.info("Selecting blob from EuroDaT storage with blobId: $blobId. Correlation id: $correlationId.")
        val blob = eurodatDataStore.selectPrivateDocument(blobId, correlationId)
        val stream = ByteArrayInputStream(blob)
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(stream))
    }
}
