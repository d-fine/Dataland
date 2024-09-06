package org.dataland.datalandbackend.interfaces.documents

import org.dataland.datalandbackend.validator.PageRange

/**
 * --- API model ---
 * Interface extending base document reference with a page and tag name in a company report
 */
interface ExtendedDocumentReference : BaseDocumentReference {

    @get:PageRange
    val page: String?

    val tagName: String?
}
