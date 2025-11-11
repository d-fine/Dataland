package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandinternalstorage.model.DocumentReferencesResponse
import org.dataland.datalandinternalstorage.repositories.BlobItemRepository
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service for handling document storage operations
 * Searches for document IDs in data points and datasets, and handles document deletion
 */
@Service
class DocumentDeletionInStorageService
    @Autowired
    constructor(
        private val blobItemRepository: BlobItemRepository,
        private val dataItemRepository: DataItemRepository,
        private val dataPointItemRepository: DataPointItemRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Retrieves references to the specified document by searching for the document ID in all data points and datasets
         *
         * @param documentId the ID of the document to search for
         * @param correlationId the correlation ID of the current user process
         * @return DocumentReferencesResponse containing datasetIds and dataPointIds that reference the document
         */
        fun getDocumentReferences(
            documentId: String,
            correlationId: String,
        ): DocumentReferencesResponse {
            logger.info("Searching for document references. DocumentId: $documentId. Correlation ID: $correlationId")

            val dataPointItems = dataPointItemRepository.findByDataPointContainingDocumentId(documentId)
            val dataPointIds: Set<String> = dataPointItems.map { it.dataPointId }.toSet()

            val dataItems = dataItemRepository.findByDataContainingDocumentId(documentId)
            val datasetIds: Set<String> = dataItems.map { it.id }.toSet()

            logger.info(
                "Found document references. DocumentId: $documentId. " +
                    "DataPoints: ${dataPointIds.size}, Datasets: ${datasetIds.size}. " +
                    "Correlation ID: $correlationId",
            )

            return DocumentReferencesResponse(
                datasetIds = datasetIds,
                dataPointIds = dataPointIds,
            )
        }

        /**
         * Deletes a document from blob storage
         * Before deletion, nullifies all file references to this document in datasets and datapoints
         *
         * @param documentId the ID of the document to delete
         * @param correlationId the correlation ID of the current user process
         */
        fun deleteDocument(
            documentId: String,
            correlationId: String,
        ) {
            logger.info("Deleting document from blob storage. DocumentId: $documentId. Correlation ID: $correlationId")

            val references = getDocumentReferences(documentId, correlationId)

            val datasetsUpdated =
                references.datasetIds.count { datasetId ->
                    nullifyFileReferencesInDataset(datasetId, documentId)
                }

            val dataPointsUpdated =
                references.dataPointIds.count { dataPointId ->
                    nullifyFileReferencesInDataPoint(dataPointId, documentId)
                }

            logger.info(
                "Nullified file references for document $documentId. " +
                    "Datasets updated: $datasetsUpdated, Data points updated: $dataPointsUpdated. " +
                    "Correlation ID: $correlationId",
            )

            blobItemRepository.deleteById(documentId)

            logger.info("Successfully deleted document. DocumentId: $documentId. Correlation ID: $correlationId")
        }

        /**
         * Nullifies file references to the specified document in a dataset
         * Handles double-encoded JSON by parsing and re-serializing at both levels
         *
         * @param datasetId the ID of the dataset to update
         * @param documentId the ID of the document whose references should be nullified
         * @return true if the dataset was updated, false otherwise
         */
        private fun nullifyFileReferencesInDataset(
            datasetId: String,
            documentId: String,
        ): Boolean {
            val dataItem = dataItemRepository.findById(datasetId).get()

            val storedData = defaultObjectMapper.readTree(dataItem.data)
            val datasetWrapper = if (storedData.isTextual) defaultObjectMapper.readTree(storedData.asText()) else storedData
            val serializedDatasetData =
                datasetWrapper.get("data")?.asText() ?: run {
                    logger.warn("Dataset $datasetId has no 'data' field")
                    return false
                }

            val datasetData = defaultObjectMapper.readTree(serializedDatasetData)
            val referencesModified = nullifyMatchingReferences(datasetData, documentId)
            val attachmentCleaned = cleanupAttachmentStructure(datasetData)

            if (referencesModified || attachmentCleaned) {
                val updatedSerializedData = defaultObjectMapper.writeValueAsString(datasetData)
                (datasetWrapper as ObjectNode).put("data", updatedSerializedData)
                val updatedWrapper = defaultObjectMapper.writeValueAsString(datasetWrapper)
                val finalData =
                    if (storedData.isTextual) defaultObjectMapper.writeValueAsString(updatedWrapper) else updatedWrapper
                val updatedDataItem = dataItem.copy(data = finalData)
                dataItemRepository.save(updatedDataItem)
                logger.info("Nullified references in dataset $datasetId")
            }

            return referencesModified || attachmentCleaned
        }

        /**
         * Nullifies file references to the specified document in a data point
         * Handles both single-encoded and double-encoded JSON formats
         *
         * @param dataPointId the ID of the data point to update
         * @param documentId the ID of the document whose references should be nullified
         * @return true if the datapoint was updated, false otherwise
         */
        private fun nullifyFileReferencesInDataPoint(
            dataPointId: String,
            documentId: String,
        ): Boolean {
            val dataPointItem = dataPointItemRepository.findById(dataPointId).get()

            val storedDataPoint = defaultObjectMapper.readTree(dataPointItem.dataPoint)
            val dataPointData =
                if (storedDataPoint.isTextual) defaultObjectMapper.readTree(storedDataPoint.asText()) else storedDataPoint
            val modified = nullifyMatchingReferences(dataPointData, documentId)

            if (modified) {
                val updatedJson = defaultObjectMapper.writeValueAsString(dataPointData)
                val finalDataPoint =
                    if (storedDataPoint.isTextual) defaultObjectMapper.writeValueAsString(updatedJson) else updatedJson
                val updatedDataPointItem = dataPointItem.copy(dataPoint = finalDataPoint)
                dataPointItemRepository.save(updatedDataPointItem)
                logger.info("Nullified references in datapoint $dataPointId")
                return true
            }

            return false
        }

        /**
         * Recursively searches for and nullifies document reference objects matching the specified document ID
         * Sets the entire dataSource object to null
         *
         * @param node the JSON node to search
         * @param documentId the document ID to match and nullify
         * @return true if any modifications were made, false otherwise
         */
        private fun nullifyMatchingReferences(
            node: JsonNode,
            documentId: String,
        ): Boolean =
            node
                .findParents("dataSource")
                .filter { it.get("dataSource")?.get("fileReference")?.asText() == documentId }
                .map { (it as ObjectNode).putNull("dataSource") }
                .isNotEmpty()

        /**
         * Cleans up attachment structure after dataSource nullification
         * If attachment.attachment.attachment has a null dataSource, nullifies the entire attachment object
         * This ensures consistency with the schema for datasets without attachments
         *
         * @param dataNode the JSON node to clean up (typically the inner dataset data)
         * @return true if the attachment structure was cleaned up, false otherwise
         */
        private fun cleanupAttachmentStructure(dataNode: JsonNode): Boolean {
            val attachmentLevel2 = dataNode.get("attachment")?.get("attachment")
            if (attachmentLevel2?.get("attachment")?.get("dataSource")?.isNull == true) {
                (attachmentLevel2 as ObjectNode).putNull("attachment")
                logger.info("Attachment cleaned")
                return true
            }
            return false
        }
    }
