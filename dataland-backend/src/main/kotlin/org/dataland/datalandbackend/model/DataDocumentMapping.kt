package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Information about the mapping between data and corresponding documents
 * @param dataId unique identifier to identify the data in the data store
 * @param documentId unique identifier to identify the document associated with the dataId
 */
data class DataDocumentMapping(
    @field:JsonProperty(required = true)
    val dataId: String,

    @field:JsonProperty()
    val documentId: String,

)
