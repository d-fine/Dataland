package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa

/**
 * --- API model ---
 * Fields describing a company report like an anual report
 */
data class CompanyReport(
    @field:JsonProperty(required = true)
    val reference: String,

    @field:JsonProperty()
    val isGroupLevel: YesNoNa? = null,
)
