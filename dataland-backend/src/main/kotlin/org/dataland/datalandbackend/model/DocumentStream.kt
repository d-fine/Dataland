package org.dataland.datalandbackend.model

import org.springframework.core.io.InputStreamResource

/**
 * --- Document model ---
 * Class for specifying a document
 * @param documentId the ID / hash of the document
 * @param type the type of document
 * @param content the content of the document as stream
 */
// TODO move to backenutils
data class DocumentStream(
    val documentId: String,
    val type: DocumentType,
    val content: InputStreamResource,
)
