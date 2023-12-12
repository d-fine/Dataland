package org.dataland.datalandbackend.model.eutaxonomy.financials

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.interfaces.frameworks.EuTaxonomyCommonFields
import org.dataland.datalandbackend.interfaces.frameworks.FrameworkBase
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceDataPoint
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import java.math.BigDecimal
import java.time.LocalDate
import java.util.EnumSet

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForFinancials framework
 */
@DataType("eutaxonomy-financials")
data class EuTaxonomyDataForFinancials(
    val financialServicesTypes: EnumSet<FinancialServicesType>? = null,

    @field:Valid
    @field:Schema(example = JsonExampleFormattingConstants.ELIGIBILITY_KPIS_DEFAULT_VALUE)
    val eligibilityKpis: Map<FinancialServicesType, EligibilityKpis>? = null,

    @field:Valid
    val creditInstitutionKpis: CreditInstitutionKpis? = null,

    @field:Valid
    val investmentFirmKpis: InvestmentFirmKpis? = null,

    @field:Valid
    val insuranceKpis: InsuranceKpis? = null,

    override val fiscalYearDeviation: FiscalYearDeviation? = null,

    override val fiscalYearEnd: LocalDate? = null,

    override val scopeOfEntities: YesNoNa? = null,

    override val nfrdMandatory: YesNo? = null,

    override val euTaxonomyActivityLevelReporting: YesNo? = null,

    @field:Valid
    override val assurance: AssuranceDataPoint? = null,

    @Min(0)
    override val numberOfEmployees: BigDecimal? = null,

    @field:Schema(example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE)
    override val referencedReports: Map<String, CompanyReport>? = null,
) : EuTaxonomyCommonFields, FrameworkBase
