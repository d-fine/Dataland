package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * dummy
 */
data class CompaniesRequestBody(@field:JsonProperty("companyName", required = true) val companyName: String = "")
