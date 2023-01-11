package org.dataland.datalandbackend.model.lksg

import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal
import java.time.LocalDate

data class General(
    val dataDate: LocalDate?,

    val lksgInScope: YesNo?,

    // val companyLegalForm: String?,

    val vatIdentificationNumber: String?,

    val numberOfEmployees: BigDecimal?,

    val shareOfTemporaryWorkers: BigDecimal?,

    val totalRevenue: BigDecimal?,

    val totalRevenueCurrency: String?,

    val listOfProductionSites: List<ProductionSite>?,
)