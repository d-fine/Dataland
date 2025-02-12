package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
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
    val documentName: String?,
    val documentCategory: DocumentCategory?,
    val companyIds: Set<String>?,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val publicationDate: LocalDate?,
    val reportingPeriod: String?,
) {
    /**
     * Returns true if all fields are null or empty; false otherwise
     */
    @JsonIgnore
    fun isNullOrEmpty() =
        documentName.isNullOrEmpty() &&
            documentCategory == null &&
            companyIds.isNullOrEmpty() &&
            publicationDate == null &&
            reportingPeriod.isNullOrEmpty()
}
