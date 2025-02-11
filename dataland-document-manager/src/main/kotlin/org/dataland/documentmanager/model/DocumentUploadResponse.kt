package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackendutils.model.DocumentCategory
import java.time.LocalDate

/**
 * --- API model ---
 * Class for giving feedback via a response to a user who tries to upload a new document.
 * @param documentId the ID of the uploaded document
 * @param documentName
 * @param documentCategory
 * @param companyIds
 * @param publicationDate
 * @param reportingPeriod
 */
data class DocumentUploadResponse(
    @field:JsonProperty(required = true)
    val documentId: String,
    @field:JsonProperty(required = false)
    val documentName: String?,
    @field:JsonProperty(required = false)
    val documentCategory: DocumentCategory?,
    @field:JsonProperty(required = false)
    val companyIds: List<String>?,
    @field:JsonProperty(required = false)
    val publicationDate: LocalDate?,
    @field:JsonProperty(required = false)
    val reportingPeriod: String?,
)
