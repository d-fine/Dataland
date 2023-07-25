package org.dataland.datalandbackend.model.sme.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.generics.Address
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the subcategory "Basic Information" belonging to the category "General" of the sme framework.
*/
data class SmeGeneralBasicInformation(
    @field:JsonProperty(required = true)
    val sector: List<String>,

    @field:JsonProperty(required = true)
    val addressOfHeadquarters: Address,

    @field:JsonProperty(required = true)
    val numberOfEmployees: BigDecimal,

    @field:JsonProperty(required = true)
    val fiscalYearStart: LocalDate,
)
