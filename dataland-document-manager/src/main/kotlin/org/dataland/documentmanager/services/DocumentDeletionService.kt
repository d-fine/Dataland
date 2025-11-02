package org.dataland.documentmanager.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.documentmanager.exceptions.DocumentNotFoundException
import org.dataland.documentmanager.repositories.DocumentMetaInfoRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service for deleting documents and ensuring referential integrity
 * @param documentMetaInfoRepository repository for document metadata
 * @param storageControllerApi client for internal storage operations
 * @param qaControllerApi client for QA service operations
 */
@Service
class DocumentDeletionService(
    @Autowired private val documentMetaInfoRepository: DocumentMetaInfoRepository,
    @Autowired private val storageControllerApi: StorageControllerApi,
    @Autowired private val qaControllerApi: QaControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Deletes a document after validating that all references are rejected
     * @param documentId the ID of the document to delete
     * @throws DocumentNotFoundException if the document does not exist
     * @throws ConflictApiException if the document has non-rejected references
     */
    fun deleteDocument(documentId: String) {
        val correlationId = UUID.randomUUID().toString()
        logger.info("Starting document deletion. DocumentId: $documentId. Correlation ID: $correlationId")

        val documentExists = documentMetaInfoRepository.existsById(documentId)
        if (!documentExists) {
            throw DocumentNotFoundException(documentId, correlationId)
        }

        val references = storageControllerApi.getDocumentReferences(documentId, correlationId)
        val dataPointIds = references["dataPointIds"] ?: emptyList()
        val datasetIds = references["datasetIds"] ?: emptyList()

        if (dataPointIds.isNotEmpty() || datasetIds.isNotEmpty()) {
            checkQaStatuses(datasetIds, dataPointIds, documentId, correlationId)
        }

        try {
            storageControllerApi.deleteDocument(documentId, correlationId)
        } catch (e: org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException) {
            logger.error("Internal Storage deletion failed. DocumentId: $documentId. Correlation ID: $correlationId", e)
            throw e
        }

        documentMetaInfoRepository.deleteById(documentId)

        logger.info("Successfully deleted document. DocumentId: $documentId. Correlation ID: $correlationId")
    }

    @Suppress("ThrowsCount")
    private fun checkQaStatuses(
        datasetIds: List<String>,
        dataPointIds: List<String>,
        documentId: String,
        correlationId: String,
    ) {
        val nonRejectedReferences = mutableListOf<String>()

        datasetIds.forEach { datasetId ->
            try {
                val qaReview = qaControllerApi.getQaReviewResponseByDataId(UUID.fromString(datasetId))
                if (qaReview.qaStatus != QaStatus.Rejected) {
                    nonRejectedReferences.add("Dataset $datasetId (status: ${qaReview.qaStatus})")
                }
            } catch (e: org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException) {
                logger.error("Failed to get QA status for dataset $datasetId. Correlation ID: $correlationId", e)
                throw e
            }
        }

        dataPointIds.forEach { dataPointId ->
            try {
                val qaReviews = qaControllerApi.getDataPointQaReviewInformationByDataId(dataPointId)
                val latestReview = qaReviews.firstOrNull()
                if (latestReview?.qaStatus != QaStatus.Rejected) {
                    nonRejectedReferences.add("Data point $dataPointId (status: ${latestReview?.qaStatus})")
                }
            } catch (e: org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException) {
                logger.error("Failed to get QA status for data point $dataPointId. Correlation ID: $correlationId", e)
                throw e
            }
        }

        if (nonRejectedReferences.isNotEmpty()) {
            throw ConflictApiException(
                summary = "Document $documentId cannot be deleted.",
                message = "Document cannot be deleted because it has active references.",
            )
        }
    }
}
