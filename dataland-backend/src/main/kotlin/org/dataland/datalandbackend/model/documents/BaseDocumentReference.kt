package org.dataland.datalandbackend.model.documents

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference

/**
 * --- API model ---
 * Fields of a generic document reference
 */
data class BaseDocumentReference(
    override val fileName: String? = null,
    @field:JsonProperty(required = true)
    override val fileReference: String,
) : BaseDocumentReference
