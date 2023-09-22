package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.interfaces.ExtendedDocumentReferenceInterface

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class ExtendedDocumentReference(
    override val page: Long? = null,
    override val tagName: String? = null,
    override val fileName: String? = null,
    // TODO check that the @field:JsonProperty(required = true) can be removed here without problems
    override val fileReference: String? = null,
) : ExtendedDocumentReferenceInterface
