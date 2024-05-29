package org.dataland.datalandbackend.model.documents

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.validator.DocumentExists
import java.time.LocalDate

/**
 * --- API model ---
 * Fields describing a company report like an annual report
 */
data class CompanyReport(
    @field:JsonProperty(required = true)
    // todo check if annotation leads to problems with OpenApi @field:NotBlank
    // todo check if annotation leads to problems with OpenApi @field:DocumentExists
    override val fileReference: String,

    override val fileName: String? = null,

    // The following annotation is required due to a known issue with the openApi generator for fields starting with is
    @field:JsonProperty()
    val isGroupLevel: YesNoNa? = null,

    val reportDate: LocalDate? = null,

    val currency: String? = null,
) : BaseDocumentReference
