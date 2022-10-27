package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class CompanyReportReference(
    @field:JsonProperty(required = true)
    val report: String,

    val page: Long? = null,

    val tagName: String? = null,

)
