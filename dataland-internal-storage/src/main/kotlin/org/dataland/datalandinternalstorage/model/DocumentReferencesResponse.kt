package org.dataland.datalandinternalstorage.model

/**
 * Response containing references to a document from datasets and data points
 * @param datasetIds set of dataset IDs that reference the document
 * @param dataPointIds set of data point IDs that reference the document
 */
data class DocumentReferencesResponse(
    val datasetIds: Set<String>,
    val dataPointIds: Set<String>,
)
