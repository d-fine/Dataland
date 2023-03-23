package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for giving feedback via a response to a user who tries to upload a new document.
 * @param documentId the id of the uploaded document
 */
data class DocumentUploadResponse(
    @field:JsonProperty(required = true)
    val documentId: String,
)
