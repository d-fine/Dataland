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
class DocumentDeletionService
    @Autowired
    constructor(
        private val documentMetaInfoRepository: DocumentMetaInfoRepository,
        private val storageControllerApi: StorageControllerApi,
        private val qaControllerApi: QaControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Deletes a document after validating that all references are rejected
         * @param documentId the ID of the document to delete
         * @throws DocumentNotFoundException if the document does not exist
         * @throws ConflictApiException if the document has active references
         */
        fun deleteDocument(documentId: String) {
            val correlationId = UUID.randomUUID().toString()
            logger.info("Attempting to delete document. DocumentId: $documentId. Correlation ID: $correlationId")

            if (!documentMetaInfoRepository.existsById(documentId)) {
                throw DocumentNotFoundException(documentId, correlationId)
            }

            val references = storageControllerApi.getDocumentReferences(documentId, correlationId)

            if ((references["datasetIds"]?.any { isDatasetActive(it, correlationId) } == true) ||
                (references["dataPointIds"]?.any { isDataPointActive(it, correlationId) } == true)
            ) {
                throw ConflictApiException(
                    summary = "Document $documentId cannot be deleted.",
                    message = "Document cannot be deleted because it has active references.",
                )
            }

            deleteFromStorage(documentId, correlationId)
            documentMetaInfoRepository.deleteById(documentId)

            logger.info("Successfully deleted document. DocumentId: $documentId. Correlation ID: $correlationId")
        }

        private fun deleteFromStorage(
            documentId: String,
            correlationId: String,
        ) {
            try {
                storageControllerApi.deleteDocument(documentId, correlationId)
            } catch (e: org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException) {
                logger.error("Internal Storage deletion failed. DocumentId: $documentId. Correlation ID: $correlationId", e)
                throw e
            }
        }

        private fun isDatasetActive(
            datasetId: String,
            correlationId: String,
        ): Boolean {
            try {
                val qaReview = qaControllerApi.getQaReviewResponseByDataId(UUID.fromString(datasetId))
                return qaReview.qaStatus != QaStatus.Rejected
            } catch (e: org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException) {
                logger.error("Failed to get QA status for dataset $datasetId. Correlation ID: $correlationId", e)
                throw e
            }
        }

        private fun isDataPointActive(
            dataPointId: String,
            correlationId: String,
        ): Boolean {
            try {
                val qaReviews = qaControllerApi.getDataPointQaReviewInformationByDataId(dataPointId)
                val latestReview = qaReviews.firstOrNull()
                return latestReview?.qaStatus != QaStatus.Rejected
            } catch (e: org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException) {
                logger.error("Failed to get QA status for data point $dataPointId. Correlation ID: $correlationId", e)
                throw e
            }
        }
    }
