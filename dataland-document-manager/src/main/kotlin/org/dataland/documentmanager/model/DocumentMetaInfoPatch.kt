package org.dataland.documentmanager.model

// import com.fasterxml.jackson.annotation.JsonFormat
import org.dataland.datalandbackendutils.model.DocumentCategory

/**
 * --- API model ---
 * Holds the meta info of a document uploaded with document
 * @param documentName
 * @param documentCategory
 * @param companyIds
 * @param publicationDate
 * @param reportingPeriods only for informative purposes
 */
data class DocumentMetaInfoPatch(
    val documentName: String?,
    val documentCategory: DocumentCategory?,
    val companyIds: List<String>?,
    // @field:JsonFormat(pattern = "yyyy-MM-dd")
    val publicationDate: String?,
    val reportingPeriods: List<String>?,
)
