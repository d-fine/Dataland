package org.dataland.datalandbackend.model.eutaxonomy

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions

/**
 * --- API model ---
 * Level of assurance for the reported data and information about the assurance provider
 */
data class AssuranceData(
    @field:JsonProperty("assurance", required = true)
    val assurance: AssuranceOptions,

    @field:JsonProperty("provider")
    val provider: String? = null,

    @field:JsonProperty("dataSource")
    val dataSource: CompanyReportReference? = null,
)
