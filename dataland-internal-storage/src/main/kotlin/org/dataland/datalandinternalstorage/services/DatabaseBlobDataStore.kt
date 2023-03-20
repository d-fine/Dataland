package org.dataland.datalandinternalstorage.services

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.repositories.BlobItemRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

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
