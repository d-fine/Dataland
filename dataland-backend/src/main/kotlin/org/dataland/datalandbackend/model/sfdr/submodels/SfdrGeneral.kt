package org.dataland.datalandbackend.model.sfdr.submodels

import java.time.LocalDate
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNoNa

/**
 * --- API model ---
 * Fields of the SFDR questionnaire regarding the impact topic "Anti-corruption and anti-bribery"
 */
data class SfdrGeneral(
    val fiscalYear: FiscalYearDeviation?,

    val fiscalYearEnd: LocalDate?,

    val annualReport: String?,

    val groupLevelAnnualReport: YesNoNa?,

    val annualReportDate: LocalDate?,

    val annualReportCurrency: String?,

    val sustainabilityReport: String?,

    val groupLevelSustainabilityReport: YesNoNa?,

    val sustainabilityReportDate: LocalDate?,

    val sustainabilityReportCurrency: String?,

    val integratedReport: String?,

    val groupLevelIntegratedReport: YesNoNa?,

    val integratedReportDate: LocalDate?,

    val integratedReportCurrency: String?,

    val esefReport: String?,

    val groupLevelEsefReport: YesNoNa?,

    val esefReportDate: LocalDate?,

    val esefReportCurrency: String?,

    val scopeOfEntities: YesNoNa?,
)
