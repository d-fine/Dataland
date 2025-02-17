package org.dataland.datalandbackend.interfaces.documents

import org.dataland.datalandbackend.model.documents.CompanyReport
import java.time.LocalDate

/**
 * --- API model ---
 * Interface of the base document reference
 */
interface BaseDocumentReference {
    val fileName: String?
    val fileReference: String?
    var publicationDate: LocalDate?

    /**
     * Converts this reference to a company report
     */
    fun toCompanyReport(): CompanyReport? =
        fileReference?.let {
            CompanyReport(
                fileName = fileName,
                fileReference = it,
                publicationDate = publicationDate,
            )
        }
}
