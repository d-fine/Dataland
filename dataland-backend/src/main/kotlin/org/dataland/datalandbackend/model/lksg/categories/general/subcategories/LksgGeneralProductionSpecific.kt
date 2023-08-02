package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production.LksgProductionSite
import org.dataland.datalandbackend.model.enums.lksg.NationalOrInternationalMarket

/**
 * --- API model ---
 * Fields of the subcategory "Production-specific" belonging to the category "General" of the lksg framework.
*/
data class LksgGeneralProductionSpecific(
      val manufacturingCompany: YesNo? = null,

      val capacity: String? = null,

      val productionViaSubcontracting: YesNo? = null,

      val subcontractingCompaniesCountries: List<String>? = null,

      val subcontractingCompaniesIndustries: List<String>? = null,

      val productionSites: YesNo? = null,

      val listOfProductionSites: List<LksgProductionSite>? = null,

      val market: NationalOrInternationalMarket? = null,

      val specificProcurement: YesNo? = null,
)
