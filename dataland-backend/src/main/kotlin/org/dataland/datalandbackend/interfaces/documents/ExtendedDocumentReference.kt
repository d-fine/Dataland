package org.dataland.datalandbackend.interfaces.documents
/**
 * --- API model ---
 * Interface extending base document reference with a page and tag name in a company report
 */
interface ExtendedDocumentReference : BaseDocumentReference {

    val page: String?

    val tagName: String?
}
