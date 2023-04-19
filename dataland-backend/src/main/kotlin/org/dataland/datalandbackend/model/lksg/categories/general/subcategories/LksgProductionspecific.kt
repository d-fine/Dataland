package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.InHouseProductionOrContractProcessing
import org.dataland.datalandbackend.model.enums.lksg.NationalOrInternationalMarket
import org.dataland.datalandbackend.model.lksg.categories.general.LksgAddress
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Production-specific"
 */data class LksgProductionspecific(
    @field:JsonProperty(required = true)
    val listOfProductionSites: YesNo,

    @field:JsonProperty(required = true)
    val capacity: BigDecimal,

    @field:JsonProperty(required = true)
    val isInhouseProductionOrIsContractProcessing: InHouseProductionOrContractProcessing,

    @field:JsonProperty(required = true)
    val subcontractingCompaniesCountries: List<String>,

    @field:JsonProperty(required = true)
    val subcontractingCompaniesIndustries: List<String>,

    @field:JsonProperty(required = true)
    val productionSites: YesNo,

    @field:JsonProperty(required = true)
    val numberOfProductionSites: BigDecimal,

    @field:JsonProperty(required = true)
    val nameOfProductionSites: List<String>,

    @field:JsonProperty(required = true)
    val addressesOfProductionSites: List<LksgAddress>,

    @field:JsonProperty(required = true)
    val listOfGoodsOrServices: List<String>,

    @field:JsonProperty(required = true)
    val market: NationalOrInternationalMarket,

    @field:JsonProperty(required = true)
    val specificProcurement: YesNo,
)
