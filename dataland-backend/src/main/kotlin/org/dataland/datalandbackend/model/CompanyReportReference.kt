package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class CompanyReportReference(

    @field:JsonProperty("report", required = true)
    val report: String,

    @field:JsonProperty("page")
    val page: BigDecimal? = null,

)
