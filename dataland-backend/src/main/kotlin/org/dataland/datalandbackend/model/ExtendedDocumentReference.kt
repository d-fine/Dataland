package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.ExtendedDocumentReferenceInterface

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class ExtendedDocumentReference(
    override val page: Long,
    override val tagName: String,
    override val fileName: String,
    @field:JsonProperty(required = true)
    override val fileReference: String
) : ExtendedDocumentReferenceInterface {

}

