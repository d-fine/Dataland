package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class CompanyReportReference(
    @field:JsonProperty(required = true)
    val report: String,
//TODO report should be aligned with the corresponding variable of DocumentReference name or reference
    val page: Long? = null,

    val tagName: String? = null,

)
