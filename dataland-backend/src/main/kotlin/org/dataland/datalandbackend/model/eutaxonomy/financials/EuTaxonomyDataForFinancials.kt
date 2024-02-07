package org.dataland.datalandbackend.model.eutaxonomy.financials

import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.AssuranceDataPoint
import org.dataland.datalandbackend.interfaces.frameworks.EuTaxonomyCommonFields
import org.dataland.datalandbackend.interfaces.frameworks.FrameworkBase
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
data class EuTaxonomyDataForFinancials(
    val financialServicesTypes: EnumSet<FinancialServicesType>? = null,

    @field:Schema(example = JsonExampleFormattingConstants.ELIGIBILITY_KPIS_DEFAULT_VALUE)
    val eligibilityKpis: Map<FinancialServicesType, EligibilityKpis>? = null,

    val creditInstitutionKpis: CreditInstitutionKpis? = null,

    val investmentFirmKpis: InvestmentFirmKpis? = null,

    val insuranceKpis: InsuranceKpis? = null,

    override val fiscalYearDeviation: FiscalYearDeviation? = null,

    override val fiscalYearEnd: LocalDate? = null,

    override val scopeOfEntities: YesNoNa? = null,

    override val nfrdMandatory: YesNo? = null,

    override val euTaxonomyActivityLevelReporting: YesNo? = null,

    override val assurance: AssuranceDataPoint? = null,

    override val numberOfEmployees: BigDecimal? = null,

    @field:Schema(example = JsonExampleFormattingConstants.REFERENCED_REPORTS_DEFAULT_VALUE)
    override val referencedReports: Map<String, CompanyReport>? = null,
) : EuTaxonomyCommonFields, FrameworkBase
