package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.FrameworkBase
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.EuTaxonomyCommonFields
import java.util.EnumSet
import java.math.BigDecimal
import java.time.LocalDate


/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForFinancials framework
 */
@DataType("eutaxonomy-financials")
data class EuTaxonomyDataForFinancials(
    @field:JsonProperty("financialServicesTypes")
    val financialServicesTypes: EnumSet<FinancialServicesType>? = null,

    @field:JsonProperty("eligibilityKpis")
    val eligibilityKpis: Map<FinancialServicesType, EligibilityKpis>? = null,

    @field:JsonProperty("creditInstitutionKpis")
    val creditInstitutionKpis: CreditInstitutionKpis? = null,

    @field:JsonProperty("insuranceKpis")
    val insuranceKpis: InsuranceKpis? = null,

    @field:JsonProperty("greenAssetRatio")
    val greenAssetRatio: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("fiscalYearDeviation")
    override val fiscalYearDeviation: YesNo? = null,

    @field:JsonProperty("fiscalYearEnd")
    override val fiscalYearEnd: LocalDate? = null,

    @field:JsonProperty("scopeOfEntities")
    override val scopeOfEntities: YesNoNa? = null,

    @field:JsonProperty("reportingObligation")
    override val reportingObligation: YesNo? = null,

    @field:JsonProperty("activityLevelReporting")
    override val activityLevelReporting: YesNo? = null,

    @field:JsonProperty("assurance")
    override val assurance: AssuranceData? = null,

    @field:JsonProperty("referencedReports")
    override val referencedReports: Map<String, CompanyReport>? = null,
) : EuTaxonomyCommonFields, FrameworkBase