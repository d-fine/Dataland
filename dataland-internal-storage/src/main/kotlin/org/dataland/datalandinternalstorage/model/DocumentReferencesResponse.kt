package org.dataland.datalandinternalstorage.model

/**
 * Response containing references to a document from datasets and data points
 * @param datasetIds list of dataset IDs that reference the document
 * @param dataPointIds list of data point IDs that reference the document
 */
data class DocumentReferencesResponse(
    val datasetIds: List<String>,
    val dataPointIds: List<String>,
)
