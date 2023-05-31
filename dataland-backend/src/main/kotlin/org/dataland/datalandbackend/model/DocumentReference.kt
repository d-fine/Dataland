package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of a generic document reference
 */
data class DocumentReference(
    @field:JsonProperty(required = true)
    val name: String,

    @field:JsonProperty(required = true)
    val reference: String,
)
