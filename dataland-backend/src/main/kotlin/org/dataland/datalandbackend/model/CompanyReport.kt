package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import java.time.LocalDate

/**
 * --- API model ---
 * Fields describing a company report like an anual report
 */
data class CompanyReport(
    @field:JsonProperty(required = true) // TODO seems reasonable, is automatically set in the frontend (at least UploadReports component)
    val reference: String,

    val isGroupLevel: YesNoNa? = null,

    val reportDate: LocalDate? = null,

    val currency: String? = null, // TODO is required in frontend, why though?
)
