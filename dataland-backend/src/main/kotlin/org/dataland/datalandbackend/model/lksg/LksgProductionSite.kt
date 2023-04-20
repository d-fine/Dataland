package org.dataland.datalandbackend.model.lksg

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.InHouseProductionOrContractProcessing
import org.dataland.datalandbackend.model.enums.lksg.NationalOrInternationalMarket
import java.math.BigDecimal

/**
 * --- API model ---
 * Production Sites for Lksg framework
 */
data class LksgProductionSite(
    val nameOfProductionSite: String?,

    val addressOfProductionSite: LksgAddress?,

    @field:JsonProperty()
    val isInHouseProductionOrIsContractProcessing: InHouseProductionOrContractProcessing?,

    val subcontractingCompaniesCountries: List<String>,

    val subcontractingCompaniesIndustries: List<String>,

    val listOfGoodsOrServices: List<String>?,

    val capacity: BigDecimal,

    val market: NationalOrInternationalMarket,

    val specificProcurement: YesNo,

    val mostImportantProducts: List<String>?,

    val productionSteps: List<String>?,

    val relatedCorporateSupplyChain: List<String>?,

    val productCategories: List<String>?,

    val definitionProductTypeService: List<String>?,

    val sourcingCountryPerCategory: List<String>?,

    val numberOfDirectSuppliers: BigDecimal?,

    val orderVolumePerProcurement: BigDecimal?,
)
