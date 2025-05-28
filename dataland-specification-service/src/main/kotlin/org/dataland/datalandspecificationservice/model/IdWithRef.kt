package org.dataland.datalandspecificationservice.model

/**
 * DTO for an ID with a reference.
 * The reference is a URL to the resource with the given ID.
 */
data class IdWithRef(
    val id: String,
    val ref: String,
)
