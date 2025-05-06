package org.dataland.documentmanager.model

import com.fasterxml.jackson.annotation.JsonFormat
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
data class DocumentMetaInfo(
    @field:JsonProperty(required = true)
    override val documentName: String,
    @field:JsonProperty(required = true)
    override val documentCategory: DocumentCategory,
    @field:JsonProperty(required = true)
    override val companyIds: Set<String>,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    override val publicationDate: LocalDate?,
    override val reportingPeriod: String?,
) : BasicDocumentMetaInfo
