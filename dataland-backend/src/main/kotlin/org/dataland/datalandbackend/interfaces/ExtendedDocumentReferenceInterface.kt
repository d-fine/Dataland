package org.dataland.datalandbackend.interfaces


/**
 * --- API model ---
 * A reference to a page in a company report
 */
interface ExtendedDocumentReferenceInterface : BaseDocumentReferenceInterface {
    val page: Long?
    val tagName: String?
}