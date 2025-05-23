// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.lksg.model.general.productionSpecific

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.frameworks.lksg.custom.LksgProductionSite
import org.dataland.datalandbackend.frameworks.lksg.model.general.productionSpecific
    .LksgGeneralProductionspecificMarketOptions
import org.dataland.datalandbackend.frameworks.lksg.model.general.productionSpecific.SpecificProcurementOptions
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.math.BigDecimal
import java.util.EnumSet

/**
 * The data-model for the ProductionSpecific section
 */
data class LksgGeneralProductionSpecific(
    val manufacturingCompany: YesNo? = null,
    val capacity: String? = null,
    val productionViaSubcontracting: YesNo? = null,
    @field:Schema(example = JsonExampleFormattingConstants.SUBCONTRACTING_COMPANIES_DEFAULT_VALUE)
    val subcontractingCompaniesCountries: Map<String, List<String>>? = null,
    val productionSites: YesNo? = null,
    val numberOfProductionSites: BigDecimal? = null,
    val listOfProductionSites: List<LksgProductionSite?>? = null,
    val market: LksgGeneralProductionspecificMarketOptions? = null,
    val specificProcurement: EnumSet<SpecificProcurementOptions>? = null,
)
