package org.dataland.documentmanager.model

// import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.model.DocumentCategory

/**
 * --- API model ---
 * Holds the meta info of a document uploaded with document
 * @param documentName
 * @param documentCategory
 * @param companyIds
 * @param publicationDate
 * @param reportingPeriod only for informative purposes
 */
data class DocumentMetaInfoPatch(
    @field:JsonProperty(required = true)
    val documentId: String,
    val documentName: String?,
    val documentCategory: DocumentCategory?,
    val companyIds: List<String>?,
    // @field:JsonFormat(pattern = "yyyy-MM-dd")
    val publicationDate: String?,
    val reportingPeriod: String?,
)
