package org.dataland.datalandbackendutils.model

import org.springframework.core.io.InputStreamResource
import java.io.IOException

/**
 * --- Document model ---
 * Class for specifying a document
 * @param documentId the ID / hash of the document
 * @param type the type of document
 * @param content the content of the document as stream
 */
data class DocumentStream(
    val documentId: String,
    val type: DocumentType,
    val content: InputStreamResource,
) {
    val contentLength: Long?
        get() = content.contentLen()
}

private fun InputStreamResource.contentLen(): Long? {
    return try {
        inputStream.use { it.readBytes().size.toLong() }
    } catch (e: IOException) {
        null
    }
}
