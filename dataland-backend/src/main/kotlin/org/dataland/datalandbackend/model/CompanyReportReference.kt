package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * A reference to a page in a company report
 */
data class CompanyReportReference(
    @field:JsonProperty(required = true)
    val report: String,

    val page: BigDecimal? = null,
)
