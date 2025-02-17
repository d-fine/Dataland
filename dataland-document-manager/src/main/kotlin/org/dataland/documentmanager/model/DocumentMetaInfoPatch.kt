package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.model.DocumentCategory
import java.time.LocalDate

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
    @field:JsonProperty(required = false)
    override val documentName: String?,
    @field:JsonProperty(required = false)
    override val documentCategory: DocumentCategory?,
    @field:JsonProperty(required = false)
    override val companyIds: Set<String>?,
    @field:JsonProperty(required = false)
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    override val publicationDate: LocalDate?,
    @field:JsonProperty(required = false)
    override val reportingPeriod: String?,
) : BasicDocumentMetaInfo {
    /**
     * Returns true if all fields are null or empty; false otherwise
     */
    @JsonIgnore
    fun isNullOrEmpty(): Boolean =
        documentName.isNullOrEmpty() &&
            documentCategory == null &&
            companyIds.isNullOrEmpty() &&
            publicationDate == null &&
            reportingPeriod.isNullOrEmpty()
}
