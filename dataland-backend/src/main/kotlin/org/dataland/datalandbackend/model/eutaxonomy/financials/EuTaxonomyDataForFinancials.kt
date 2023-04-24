package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.FrameworkBase
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.dataland.datalandbackend.model.eutaxonomy.EuTaxonomyCommonFields
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

    @field:Schema(example = JsonExampleFormattingConstants.ELIGIBILITY_KPIS_DEFAULT_VALUE)
    val eligibilityKpis: Map<FinancialServicesType, EligibilityKpis>? = null,

    val creditInstitutionKpis: CreditInstitutionKpis? = null,

    val investmentFirmKpis: InvestmentFirmKpis? = null,

    val insuranceKpis: InsuranceKpis? = null,

    @field:JsonProperty(required = true)
    override val fiscalYearDeviation: FiscalYearDeviation,

    @field:JsonProperty(required = true)
    override val fiscalYearEnd: LocalDate,

    override val scopeOfEntities: YesNoNa? = null,

    override val reportingObligation: YesNo? = null,

    override val activityLevelReporting: YesNo? = null,

    override val assurance: AssuranceData? = null,

    @field:JsonProperty(required = true)
    override val numberOfEmployees: BigDecimal,

    override val referencedReports: Map<String, CompanyReport>? = null,
) : EuTaxonomyCommonFields, FrameworkBase
