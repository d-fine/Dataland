package org.dataland.documentmanager.model

/**
 * --- Document model ---
 * Class for specifying a document
 * @param title the display title of the document
 * @param content the content of the document as byte array
 */
data class Document(
    val title: String,
    val content: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Document

        if (title != other.title) return false
        if (!content.contentEquals(other.content)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + content.contentHashCode()
        return result
    }
}
