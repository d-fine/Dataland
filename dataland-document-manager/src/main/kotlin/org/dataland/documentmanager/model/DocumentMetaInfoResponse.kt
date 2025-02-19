package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonFormat
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
data class DocumentMetaInfoResponse(
    @field:JsonProperty(required = true)
    val documentId: String,
    override val documentName: String?,
    override val documentCategory: DocumentCategory?,
    override val companyIds: Set<String>?,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    override val publicationDate: LocalDate?,
    override val reportingPeriod: String?,
) : BasicDocumentMetaInfo
