package org.dataland.datalandbackend.model.sfdr.submodels

import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Anti-corruption and anti-bribery"
 */
data class SfdrGeneral(
    val fiscalYear: FiscalYearDeviation? = null,

    val fiscalYearEnd: LocalDate? = null,

    val annualReport: String? = null,

    val groupLevelAnnualReport: YesNoNa? = null,

    val annualReportDate: LocalDate? = null,

    val annualReportCurrency: String? = null,

    val sustainabilityReport: String? = null,

    val groupLevelSustainabilityReport: YesNoNa? = null,

    val sustainabilityReportDate: LocalDate? = null,

    val sustainabilityReportCurrency: String? = null,

    val integratedReport: String? = null,

    val groupLevelIntegratedReport: YesNoNa? = null,

    val integratedReportDate: LocalDate? = null,

    val integratedReportCurrency: String? = null,

    val esefReport: String? = null,

    val groupLevelEsefReport: YesNoNa? = null,

    val esefReportDate: LocalDate? = null,

    val esefReportCurrency: String? = null,

    val scopeOfEntities: YesNoNa? = null,
)
