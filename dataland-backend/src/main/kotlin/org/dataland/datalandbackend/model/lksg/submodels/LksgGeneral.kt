package org.dataland.datalandbackend.model.lksg.submodels

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.lksg.ProductionSite
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "General"
 */
data class LksgGeneral(
    @field:JsonProperty(required = true)
    val dataDate: LocalDate,

    @field:JsonProperty(required = true)
    val lksgInScope: YesNo,

    @field:JsonProperty(required = true)
    val vatIdentificationNumber: String,

    @field:JsonProperty(required = true)
    val numberOfEmployees: BigDecimal,

    @field:JsonProperty(required = true)
    val shareOfTemporaryWorkers: BigDecimal,

    @field:JsonProperty(required = true)
    val totalRevenue: BigDecimal,

    @field:JsonProperty(required = true)
    val totalRevenueCurrency: String,

    @field:JsonProperty(required = true)
    val listOfProductionSites: List<ProductionSite>,
)
