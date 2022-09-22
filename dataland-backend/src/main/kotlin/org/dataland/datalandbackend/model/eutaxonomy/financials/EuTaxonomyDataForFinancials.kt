package org.dataland.datalandbackend.model.eutaxonomy.financials

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.FrameworkBase
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.dataland.datalandbackend.model.eutaxonomy.EuTaxonomyCommonFields
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

    val eligibilityKpis: Map<FinancialServicesType, EligibilityKpis>? = null,

    val creditInstitutionKpis: CreditInstitutionKpis? = null,

    val insuranceKpis: InsuranceKpis? = null,

    val greenAssetRatio: DataPoint<BigDecimal>? = null,

    override val fiscalYearDeviation: YesNo? = null,

    override val fiscalYearEnd: LocalDate? = null,

    override val scopeOfEntities: YesNoNa? = null,

    override val reportingObligation: YesNo? = null,

    override val activityLevelReporting: YesNo? = null,

    override val assurance: AssuranceData? = null,

    override val referencedReports: Map<String, CompanyReport>? = null,
) : EuTaxonomyCommonFields, FrameworkBase
