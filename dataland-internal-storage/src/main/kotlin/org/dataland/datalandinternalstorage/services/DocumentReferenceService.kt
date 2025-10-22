package org.dataland.datalandinternalstorage.services

import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service for handling document reference queries
 * Searches for document IDs in data points and datasets
 */
@Service
class DocumentReferenceService(
    @Autowired private val entityManager: EntityManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Retrieves references to the specified document by searching for the document ID in all data points and datasets
     *
     * @param documentId the ID of the document to search for
     * @param correlationId the correlation ID of the current user process
     * @return a map with two keys:
     *           - "dataPointIds": list of data point IDs referencing this document
     *           - "datasetIds": list of dataset IDs referencing this document
     */
    fun getDocumentReferences(
        documentId: String,
        correlationId: String,
    ): Map<String, List<String>> {
        logger.info("Searching for document references. DocumentId: $documentId. Correlation ID: $correlationId")
        val dataPointQuery =
            entityManager.createQuery(
                "SELECT d.dataPointId FROM DataPointItem d WHERE d.dataPoint LIKE :documentId",
                String::class.java,
            )
        dataPointQuery.setParameter("documentId", "%$documentId%")
        val dataPointIds = dataPointQuery.resultList

        val datasetQuery =
            entityManager.createQuery(
                "SELECT d.id FROM DataItem d WHERE d.data LIKE :documentId",
                String::class.java,
            )
        datasetQuery.setParameter("documentId", "%$documentId%")
        val datasetIds = datasetQuery.resultList

        return mapOf(
            "dataPointIds" to dataPointIds,
            "datasetIds" to datasetIds,
        )
    }
}
