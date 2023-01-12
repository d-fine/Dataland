package org.dataland.datalandbackend.model.lksg.submodels

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.lksg.ProductionSite
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "General"
 */
data class LksgGeneral(
    val dataDate: LocalDate? = null,

    val lksgInScope: YesNo? = null,

    val vatIdentificationNumber: String? = null,

    val numberOfEmployees: BigDecimal? = null,

    val shareOfTemporaryWorkers: BigDecimal? = null,

    val totalRevenue: BigDecimal? = null,

    val totalRevenueCurrency: String? = null,

    val listOfProductionSites: List<ProductionSite>? = null,
)
