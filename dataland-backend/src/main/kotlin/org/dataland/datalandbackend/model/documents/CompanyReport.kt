package org.dataland.datalandbackend.model.documents

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalandbackend.validator.DocumentExists
import java.time.LocalDate

/**
 * --- API model ---
 * Fields describing a company report like an annual report
 */
data class CompanyReport(
    @field:JsonProperty(required = true)
    @field:NotBlank
    @field:DocumentExists
    override val fileReference: String,

    override val fileName: String? = null,

    val publicationDate: LocalDate? = null,
) : BaseDocumentReference
