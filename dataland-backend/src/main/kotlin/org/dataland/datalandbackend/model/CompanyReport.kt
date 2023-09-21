package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.interfaces.BaseDocumentReferenceInterface
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import java.time.LocalDate

/**
 * --- API model ---
 * Fields describing a company report like an annual report
 */
data class CompanyReport(
    @field:JsonProperty(required = true)
    override val fileReference: String,

    override val fileName: String? = null,

    // The following annotation is required due to a known issue with the openApi generator for fields starting with is
    @field:JsonProperty()
    val isGroupLevel: YesNoNa? = null,

    val reportDate: LocalDate? = null,

    val currency: String? = null,
): BaseDocumentReferenceInterface
