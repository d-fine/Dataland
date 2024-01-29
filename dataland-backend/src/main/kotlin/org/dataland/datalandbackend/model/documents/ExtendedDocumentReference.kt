package org.dataland.datalandbackend.model.documents

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import org.dataland.datalandbackend.interfaces.documents.ExtendedDocumentReference

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class ExtendedDocumentReference(
    override val page: Long? = null,
    override val tagName: String? = null,
    override val fileName: String? = null,
    @field:JsonProperty(required = true)
    @field:NotBlank
    override val fileReference: String,
) : ExtendedDocumentReference
