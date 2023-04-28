package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.InHouseProductionOrContractProcessing
import org.dataland.datalandbackend.model.enums.lksg.NationalOrInternationalMarket
import org.dataland.datalandbackend.model.lksg.LksgProductionSite
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Grievance mechanism - Own operations"
 */
data class LksgProductionSpecific(

    val manufacturingCompany: YesNo?,

    val capacity: BigDecimal?,

    @field:JsonProperty()
    val isInHouseProductionOrIsContractProcessing: InHouseProductionOrContractProcessing?,

    val subcontractingCompaniesCountries: String?,

    val subcontractingCompaniesIndustries: String?,

    val listOfProductionSites: List<LksgProductionSite>?,

    val market: NationalOrInternationalMarket?,

    val specificProcurement: YesNo?,
)
