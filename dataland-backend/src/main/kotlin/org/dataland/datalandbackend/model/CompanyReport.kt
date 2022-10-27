package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import java.time.LocalDate

/**
 * --- API model ---
 * Fields describing a company report like an anual report
 */
data class CompanyReport(
    @field:JsonProperty(required = true)
    val reference: String,

    @field:JsonProperty()
    val isGroupLevel: YesNoNa? = null,

    val reportDate: LocalDate? = null,

    val currency: String? = null,
)
