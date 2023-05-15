package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.lksg.NationalOrInternationalMarket
import org.dataland.datalandbackend.model.lksg.LksgProductionSite
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Production Specific"
 */
data class LksgProductionSpecific(

    val manufacturingCompany: BaseDataPoint<YesNo>?,

    val capacity: BaseDataPoint<BigDecimal>?,

    @field:JsonProperty()
    val isContractProcessing: BaseDataPoint<YesNo>?,

    val subcontractingCompaniesCountries: BaseDataPoint<List<String>>?,

    val subcontractingCompaniesIndustries: BaseDataPoint<List<String>>?,

    val productionSites: BaseDataPoint<YesNo>?,

    val listOfProductionSites: BaseDataPoint<List<LksgProductionSite>>?,

    val market: BaseDataPoint<NationalOrInternationalMarket>?,

    val specificProcurement: BaseDataPoint<YesNo>?,
)
