package org.dataland.datalandinternalstorage.controller

import org.dataland.datalandinternalstorage.api.StorageAPI
import org.dataland.datalandinternalstorage.model.StorableDataPoint
import org.dataland.datalandinternalstorage.services.DatabaseBlobDataStore
import org.dataland.datalandinternalstorage.services.DatabaseStringDataStore
import org.dataland.datalandinternalstorage.services.DocumentStorageService
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
 * @param documentStorageService service for handling document storage operations
 */
@RestController
@Component("StorageController")
class StorageController(
    @Autowired val stringDataStore: DatabaseStringDataStore,
    @Autowired val blobDataStore: DatabaseBlobDataStore,
    @Autowired val documentStorageService: DocumentStorageService,
) : StorageAPI {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun selectDataById(
        dataId: String,
        correlationId: String,
    ): ResponseEntity<String> {
        logger.info("Selecting data from database with data ID: $dataId. Correlation ID: $correlationId.")
        return ResponseEntity.ok(stringDataStore.selectDataset(dataId, correlationId))
    }

    override fun selectBlobById(
        blobId: String,
        correlationId: String,
    ): ResponseEntity<InputStreamResource> {
        logger.info("Selecting blob from database with hash: $blobId. Correlation id: $correlationId.")
        val blob = blobDataStore.selectBlobById(blobId, correlationId)
        val stream = ByteArrayInputStream(blob)
        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(stream))
    }

    override fun selectBatchDataPointsByIds(
        dataIds: List<String>,
        correlationId: String,
    ): ResponseEntity<Map<String, StorableDataPoint>> {
        logger.info("Selecting ${dataIds.size} data points from the database: $dataIds. Correlation id: $correlationId.")
        return ResponseEntity.ok(stringDataStore.selectDataPoints(dataIds, correlationId))
    }

    override fun getDocumentReferences(
        documentId: String,
        correlationId: String,
    ): ResponseEntity<Map<String, List<String>>> {
        logger.info("Retrieving document references for: $documentId. Correlation id: $correlationId.")
        return ResponseEntity.ok(documentStorageService.getDocumentReferences(documentId, correlationId))
    }

    override fun deleteDocument(
        documentId: String,
        correlationId: String,
    ): ResponseEntity<Unit> {
        logger.info("Deleting document: $documentId. Correlation id: $correlationId.")
        documentStorageService.deleteDocument(documentId, correlationId)
        return ResponseEntity.noContent().build()
    }
}
