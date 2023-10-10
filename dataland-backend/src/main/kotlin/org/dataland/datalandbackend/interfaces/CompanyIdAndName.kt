package org.dataland.datalandbackend.interfaces

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Interface containing only a company's id and name
 */
interface CompanyIdAndName {
    @get:JsonProperty(required = true)
    val companyId: String

    @get:JsonProperty(required = true)
    val companyName: String
}
