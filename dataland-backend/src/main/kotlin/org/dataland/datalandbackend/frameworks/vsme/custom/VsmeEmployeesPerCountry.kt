package org.dataland.datalandbackend.frameworks.vsme.custom

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * Employees per country class for vsme framework
 */
data class VsmeEmployeesPerCountry(
    @field:JsonProperty(required = true)
    val country: String,
    val numberOfEmployeesInHeadCount: Int? = null,
    val numberOfEmployeesInFullTimeEquivalent: BigDecimal? = null,
)
