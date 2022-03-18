package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Class for defining the request body of a request containing only a company name
 * @param companyName name of the company
 */
data class CompaniesRequestBody(@field:JsonProperty("companyName", required = true) val companyName: String = "")
