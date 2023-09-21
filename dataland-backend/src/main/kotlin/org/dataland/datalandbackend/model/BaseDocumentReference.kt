package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.BaseDocumentReferenceInterface

/**
 * --- API model ---
 * Fields of a generic document reference
 */
data class BaseDocumentReference(
    override val fileName: String,
    @field:JsonProperty(required = true)
    override val fileReference: String,
) : BaseDocumentReferenceInterface

