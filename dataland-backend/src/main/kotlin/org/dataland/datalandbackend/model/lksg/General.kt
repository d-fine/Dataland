package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "General"
 */
data class General(
    val dataDate: LocalDate?,

    val lksgInScope: YesNo?,

    val vatIdentificationNumber: String?,

    val numberOfEmployees: BigDecimal?,

    val shareOfTemporaryWorkers: BigDecimal?,

    val totalRevenue: BigDecimal?,

    val totalRevenueCurrency: String?,

    val listOfProductionSites: List<ProductionSite>?,
)