package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding a single product category for the "Product Categories" field
 */
data class LksgProcurementCategory(
    @field:JsonProperty(required = true)
    val definitionProductTypeService: List<String>,

    val numberOfSuppliersPerCountry: Map<String, Int?>?,

    val orderVolume: BigDecimal?,
)
