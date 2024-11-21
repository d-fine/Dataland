package org.dataland.datalandbackend.frameworks.lksg.custom

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding a single procurement category for the "Procurement Categories" field
 */
data class LksgProcurementCategory(
    @field:JsonProperty(required = true)
    val procuredProductTypesAndServicesNaceCodes: List<String>,
    val numberOfSuppliersPerCountryCode: Map<String, Int?>?,
    val shareOfTotalProcurementInPercent: BigDecimal?,
)
