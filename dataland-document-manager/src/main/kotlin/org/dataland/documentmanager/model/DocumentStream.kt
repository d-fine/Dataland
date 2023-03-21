package org.dataland.documentmanager.model

import org.springframework.core.io.InputStreamResource

/**
 * --- Document model ---
 * Class for specifying a document
 * @param title the display title of the document
 * @param content the content of the document as byte array
 */
data class DocumentStream (
    val title: String,
    val content: ByteArray,
) {
}
