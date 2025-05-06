package org.dataland.documentmanager.model

import org.dataland.datalandbackendutils.model.DocumentCategory
import java.time.LocalDate

/**
 * Interface for DocumentMetaInfo Patch and Response Objects
 */
interface BasicDocumentMetaInfo {
    val documentName: String?
    val documentCategory: DocumentCategory?
    val companyIds: Set<String>?
    val publicationDate: LocalDate?
    val reportingPeriod: String?
}
