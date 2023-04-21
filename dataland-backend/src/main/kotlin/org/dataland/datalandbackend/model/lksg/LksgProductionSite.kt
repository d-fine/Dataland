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

    val subcontractingCompaniesCountries: String?,

    val subcontractingCompaniesIndustries: String?,

    val listOfGoodsOrServices: List<String>?,

    val capacity: BigDecimal,

    val market: NationalOrInternationalMarket,

    val specificProcurement: YesNo?,

    val mostImportantProducts: String?,

    val productionSteps: String?,

    val relatedCorporateSupplyChain: String?,

    val productCategories: String?,

    val definitionProductTypeService: String?,

    val sourcingCountryPerCategory: String?,

    val numberOfDirectSuppliers: BigDecimal?,

    val orderVolumePerProcurement: BigDecimal?,
)
