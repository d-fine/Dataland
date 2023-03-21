package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- Document manager model ---
 * Class for giving feedback via a response to a user who tries to upload an existing document.
 * @param documentExists defines if the document already exists
 */
data class DocumentExistsResponse (
    @field:JsonProperty(required = true)
    val documentExists: Boolean
)