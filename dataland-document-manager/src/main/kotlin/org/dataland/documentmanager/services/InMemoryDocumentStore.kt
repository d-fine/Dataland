package org.dataland.documentmanager.services

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * A service that can be used to store binary blobs (documents) in memory
 */
@Component
class InMemoryDocumentStore {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataInMemoryStorage = mutableMapOf<String, ByteArray>()

    /**
     * Stores the provided dataset in memory identified by its sha256 hash.
     */
    fun storeDataInMemory(documentId: String, data: ByteArray) {
        dataInMemoryStorage[documentId] = data
        logger.debug("Stored blob with hash $documentId to the in-memory store")
    }

    /**
     * Retrieves the data identified by the given hash from the in-memory store.
     */
    fun retrieveDataFromMemoryStore(documentId: String): ByteArray? {
        return dataInMemoryStorage[documentId]
    }

    /**
     * Deletes a dataset from memory. Returns the previously stored dataset if it exists.
     */
    fun deleteFromInMemoryStore(documentId: String): ByteArray? {
        logger.debug("Deleting blob with hash $documentId from the in-memory store")
        return dataInMemoryStorage.remove(documentId)
    }
}
