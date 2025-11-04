package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
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
class DocumentStorageService(
    @Autowired private val blobItemRepository: BlobItemRepository,
    @Autowired private val dataItemRepository: DataItemRepository,
    @Autowired private val dataPointItemRepository: DataPointItemRepository,
    @Autowired private val objectMapper: ObjectMapper,
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

        val allDataPoints = dataPointItemRepository.findAll()
        val dataPointIds: List<String> =
            allDataPoints
                .filter { containsDocumentReference(it.dataPoint, documentId) }
                .map { it.dataPointId }

        val allDatasets = dataItemRepository.findAll()
        val datasetIds: List<String> =
            allDatasets
                .filter { containsDocumentReference(it.data, documentId) }
                .map { it.id }

        logger.info(
            "Found document references. DocumentId: $documentId. " +
                "DataPoints: ${dataPointIds.size}, Datasets: ${datasetIds.size}. " +
                "Correlation ID: $correlationId",
        )

        return mapOf(
            "dataPointIds" to dataPointIds,
            "datasetIds" to datasetIds,
        )
    }

    /**
     * Checks if the given JSON data contains a reference to the specified document ID
     * Handles datasets (wrapped with "data" field) and data points (direct content)
     *
     * @param jsonData the JSON data string from the database
     * @param documentId the document ID to search for
     * @return true if the data contains a reference to the document, false otherwise
     */
    private fun containsDocumentReference(
        jsonData: String,
        documentId: String,
    ): Boolean =
        try {
            val parsedJson = objectMapper.readTree(jsonData)

            if (parsedJson.has("data") && parsedJson.get("data").isTextual) {
                val innerDataString = parsedJson.get("data").asText()
                innerDataString.contains(documentId)
            } else {
                jsonData.contains(documentId)
            }
        } catch (e: com.fasterxml.jackson.core.JsonProcessingException) {
            logger.warn("Failed to parse JSON data while searching for document $documentId", e)
            false
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
            references["datasetIds"]?.count { datasetId ->
                nullifyFileReferencesInDataset(datasetId, documentId)
            } ?: 0

        val dataPointsUpdated =
            references["dataPointIds"]?.count { dataPointId ->
                nullifyFileReferencesInDataPoint(dataPointId, documentId)
            } ?: 0

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
    @Suppress("ReturnCount")
    private fun nullifyFileReferencesInDataset(
        datasetId: String,
        documentId: String,
    ): Boolean {
        val dataItem =
            dataItemRepository.findById(datasetId).orElse(null) ?: run {
                logger.warn("Dataset $datasetId not found")
                return false
            }

        val root = objectMapper.readTree(dataItem.data)
        val outerJson = if (root.isTextual) objectMapper.readTree(root.asText()) else root
        val innerDataString =
            outerJson.get("data")?.asText() ?: run {
                logger.warn("Dataset $datasetId has no 'data' field")
                return false
            }

        val innerJsonNode = objectMapper.readTree(innerDataString)
        val modified = nullifyMatchingReferences(innerJsonNode, documentId)

        if (modified) {
            forceNullHasAttachment(innerJsonNode)

            val updatedInnerData = objectMapper.writeValueAsString(innerJsonNode)
            (outerJson as ObjectNode).put("data", updatedInnerData)
            val updatedOuterJson = objectMapper.writeValueAsString(outerJson)
            val finalData = if (root.isTextual) objectMapper.writeValueAsString(updatedOuterJson) else updatedOuterJson
            val updatedDataItem = dataItem.copy(data = finalData)
            dataItemRepository.save(updatedDataItem)
            logger.info("Nullified references and attachments in dataset $datasetId")
            return true
        }

        return false
    }

    /**
     * Nullifies file references to the specified document in a data point
     * Handles both single-encoded and double-encoded JSON formats
     *
     * @param dataPointId the ID of the data point to update
     * @param documentId the ID of the document whose references should be nullified
     * @return true if the datapoint was updated, false otherwise
     */
    @Suppress("ReturnCount")
    private fun nullifyFileReferencesInDataPoint(
        dataPointId: String,
        documentId: String,
    ): Boolean {
        val dataPointItem =
            dataPointItemRepository.findById(dataPointId).orElse(null) ?: run {
                logger.warn("Datapoint $dataPointId not found")
                return false
            }

        val root = objectMapper.readTree(dataPointItem.dataPoint)
        val targetNode = if (root.isTextual) objectMapper.readTree(root.asText()) else root
        val modified = nullifyMatchingReferences(targetNode, documentId)

        if (modified) {
            val updatedJson = objectMapper.writeValueAsString(targetNode)
            val finalDataPoint = if (root.isTextual) objectMapper.writeValueAsString(updatedJson) else updatedJson
            val updatedDataPointItem = dataPointItem.copy(dataPoint = finalDataPoint)
            dataPointItemRepository.save(updatedDataPointItem)
            logger.info("Nullified references in datapoint $dataPointId")
            return true
        }

        return false
    }

    /**
     * Checks if a JSON node contains a fileReference field matching the specified document ID
     *
     * @param node the JSON node to check
     * @param documentId the document ID to match
     * @return true if the node has a matching fileReference, false otherwise
     */
    private fun hasMatchingFileReference(
        node: JsonNode,
        documentId: String,
    ): Boolean {
        if (!node.isObject || !node.has("fileReference")) return false
        val fileRefNode = node.get("fileReference")
        return fileRefNode != null && fileRefNode.isTextual && fileRefNode.asText() == documentId
    }

    /**
     * Processes a single field in an object node, checking and nullifying matching file references
     *
     * @param objectNode the parent object node
     * @param fieldName the name of the field being processed
     * @param value the value of the field
     * @param documentId the document ID to match
     * @return true if any modifications were made, false otherwise
     */
    private fun processObjectField(
        objectNode: ObjectNode,
        fieldName: String,
        value: JsonNode,
        documentId: String,
    ): Boolean =
        when {
            value.isObject -> {
                if (hasMatchingFileReference(value, documentId)) {
                    objectNode.putNull(fieldName)
                    true
                } else {
                    nullifyMatchingReferences(value, documentId)
                }
            }
            value.isArray -> nullifyMatchingReferences(value, documentId)
            else -> false
        }

    /**
     * Processes a single element in an array, checking and nullifying matching file references
     *
     * @param arrayNode the parent array node
     * @param index the index of the element being processed
     * @param element the element to process
     * @param documentId the document ID to match
     * @return true if any modifications were made, false otherwise
     */
    private fun processArrayElement(
        arrayNode: com.fasterxml.jackson.databind.node.ArrayNode,
        index: Int,
        element: JsonNode,
        documentId: String,
    ): Boolean {
        if (hasMatchingFileReference(element, documentId)) {
            arrayNode.set(
                index,
                com.fasterxml.jackson.databind.node.NullNode
                    .getInstance(),
            )
            return true
        }
        return nullifyMatchingReferences(element, documentId)
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
    ): Boolean {
        var modified = false

        when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                objectNode.properties().forEach { (fieldName, value) ->
                    modified = processObjectField(objectNode, fieldName, value, documentId) || modified
                }
            }
            node.isArray -> {
                val arrayNode = node as com.fasterxml.jackson.databind.node.ArrayNode
                for (i in 0 until arrayNode.size()) {
                    modified = processArrayElement(arrayNode, i, arrayNode.get(i), documentId) || modified
                }
            }
        }

        return modified
    }

    /**
     * Sets the attachment field to null in LkSG datasets
     * Navigates through the nested structure: attachment -> attachment -> attachment (leaf)
     * This changes "Has Attachment" from "Yes" to null when documents are deleted
     *
     * @param dataNode the JSON node to update (typically the inner dataset data)
     * @return true if the attachment field was found and nullified, false otherwise
     */
    private fun forceNullHasAttachment(dataNode: JsonNode): Boolean {
        val att0 = dataNode.get("attachment")
        if (att0 != null && att0.isObject) {
            val att1 = att0.get("attachment")
            if (att1 != null && att1.isObject) {
                val att1Obj = att1 as ObjectNode
                if (att1Obj.has("attachment")) {
                    att1Obj.putNull("attachment")
                    return true
                }
            }
        }

        var changed = false
        when {
            dataNode.isObject -> {
                val obj = dataNode as ObjectNode
                obj.properties().forEach { (_, v) ->
                    if (forceNullHasAttachment(v)) {
                        changed = true
                    }
                }
            }
            dataNode.isArray -> {
                dataNode.forEach {
                    if (forceNullHasAttachment(it)) {
                        changed = true
                    }
                }
            }
        }
        return changed
    }
}
