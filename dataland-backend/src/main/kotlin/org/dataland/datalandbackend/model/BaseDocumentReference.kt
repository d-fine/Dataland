package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of a generic document reference
 */
interface BaseDocumentReferenceInterface {
    val fileName: String?
    val fileReference: String
}
/**
 * --- API model ---
 * Fields of a generic document reference
 */
data class BaseDocumentReference(
    @field:JsonProperty(required = true)
    override val fileName: String,
    @field:JsonProperty(required = true)
    override val fileReference: String,
): BaseDocumentReferenceInterface

