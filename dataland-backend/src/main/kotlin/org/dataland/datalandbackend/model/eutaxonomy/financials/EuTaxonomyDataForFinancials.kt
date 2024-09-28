package org.dataland.datalandbackend.model.eutaxonomy.financials

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.AssuranceDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.math.BigDecimal
import java.time.LocalDate
import java.util.EnumSet

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForFinancials framework
 */
@Suppress("MagicNumber")
@DataType("eutaxonomy-financials", 1)
@ValidateReferencedReportsListForEuTaxonomyFinancials
data class EuTaxonomyDataForFinancials(
    val financialServicesTypes: EnumSet<FinancialServicesType>? = null,
    @field:Schema(example = JsonExampleFormattingConstants.ELIGIBILITY_KPIS_DEFAULT_VALUE)
    @field:Valid()
    val eligibilityKpis: Map<FinancialServicesType, EligibilityKpis>? = null,
    @field:Valid()
    val creditInstitutionKpis: CreditInstitutionKpis? = null,
    @field:Valid()
    val investmentFirmKpis: InvestmentFirmKpis? = null,
    @field:Valid()
    val insuranceKpis: InsuranceKpis? = null,
    val fiscalYearDeviation: FiscalYearDeviation? = null,
    val fiscalYearEnd: LocalDate? = null,
    val scopeOfEntities: YesNoNa? = null,
    val nfrdMandatory: YesNo? = null,
    val euTaxonomyActivityLevelReporting: YesNo? = null,
    @field:Valid()
    val assurance: AssuranceDataPoint? = null,
    val numberOfEmployees: BigDecimal? = null,
    @field:Valid
    @field:Schema(example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE)
    val referencedReports: Map<String, CompanyReport>? = null,
)
