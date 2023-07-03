package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Fields of the LKSG questionnaire describing the number of suppliers a company has per country
 */
data class LksgCountryAssociatedSuppliers(
    @field:JsonProperty(required = true)
    val country: String,

    val numberOfSuppliers: Int?,
)
