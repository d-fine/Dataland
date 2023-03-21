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
    fun storeDataInMemory(hash: String, data: ByteArray): String {
        dataInMemoryStorage[hash] = data
        logger.debug("Stored blob with hash $hash to the in-memory store")
        return hash
    }

    /**
     * Retrieves the data identified by the given hash from the in-memory store.
     * Throws a ResourceNotFoundException if no such dataset exists.
     */
    fun retrieveDataFromMemoryStore(sha256hash: String): ByteArray {
        return dataInMemoryStorage[sha256hash]
            ?: throw ResourceNotFoundApiException(
                "Blob for hash \"$sha256hash\" not found in temporary storage",
                "Dataland does not know the file identified by \"$sha256hash\"",
            )
    }

    /**
     * Deletes a dataset from memory. Returns the previously stored dataset if it exists.
     */
    fun deleteFromInMemoryStore(sha256hash: String): ByteArray? {
        logger.debug("Deleting blob with hash $sha256hash from the in-memory store")
        return dataInMemoryStorage.remove(sha256hash)
    }
}
