package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.datalandinternalstorage.entities.BlobItem
import org.dataland.datalandinternalstorage.repositories.BlobItemRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Simple implementation of a data store for blobs using JPA
 * @param blobItemRepository the JPA repository for storing blobs
 */
@Component
class DatabaseBlobDataStore(
    @Autowired private val blobItemRepository: BlobItemRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Stores the provided binary blob to the database and returns the
     * sha256 hash of the blob. Also ensures that this function is not executed as part of any transaction.
     * This will guarantee that the write is commited after exit of this method.
     * @param blob the blob to store to the database
     * @return the sha256 hash of the blob under which is it now accessible in the database
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobToDatabase(blob: ByteArray): String {
        val hash = blob.sha256()
        val blobItem = BlobItem(hash, blob)
        blobItemRepository.save(blobItem)
        return hash
    }

    /**
     * Retrieves the blob data from the database
     * @param sha256hash the hash of the data to be retrieved
     * @return the blob retrieved from the database
     */
    fun selectBlobByHash(sha256hash: String, correlationId: String): ByteArray {
        return blobItemRepository.findById(sha256hash).orElseThrow {
            logger.info("Blob with hash: $sha256hash could not be found. Correlation id: $correlationId.")
            ResourceNotFoundApiException(
                "Dataset not found",
                "No blob with the hash: $sha256hash could be found in the data store.",
            )
        }.data
    }
}
