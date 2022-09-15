package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class CompanyReportReference(
    @field:JsonProperty("page", required = true)
    val page: Int,

    @field:JsonProperty("report", required = true)
    val report: String
)
