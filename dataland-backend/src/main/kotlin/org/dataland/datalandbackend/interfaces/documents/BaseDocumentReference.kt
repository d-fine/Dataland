package org.dataland.datalandbackend.interfaces.documents

import java.time.LocalDate

/**
 * --- API model ---
 * Interface of the base document reference
 */
interface BaseDocumentReference {
    val fileName: String?
    val fileReference: String?
    val publicationDate: LocalDate?
}
