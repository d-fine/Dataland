package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of a generic document reference
 */
data class DocumentReference(
    @field:JsonProperty(required = true)
    val name: String,
    //TODO make naming clearer, maybe fileName and fileReference
    @field:JsonProperty(required = true)
    val reference: String,
)
