package org.dataland.datalandbackend.frameworks.sme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * Employees per country class for vsme framework
 */
data class SmeEmployeesPerCountry(
    @field:JsonProperty(required = true)
    val country: String,

    val numberOfEmployeesInHeadCount: Int? = null,

    val numberOfEmployeesInFullTimeEquivalent: BigDecimal? = null,

)
