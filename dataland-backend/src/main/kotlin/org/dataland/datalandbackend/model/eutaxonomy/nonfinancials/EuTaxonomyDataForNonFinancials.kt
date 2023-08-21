package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.FrameworkBase
import org.dataland.datalandbackend.model.enums.commons.FiscalYearDeviation
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.enums.commons.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.dataland.datalandbackend.model.eutaxonomy.EuTaxonomyCommonFields
import java.math.BigDecimal
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */
@DataType("eutaxonomy-non-financials")
data class EuTaxonomyDataForNonFinancials(
    val capex: EuTaxonomyDetailsPerCashFlowType? = null,

    val opex: EuTaxonomyDetailsPerCashFlowType? = null,

    val revenue: EuTaxonomyDetailsPerCashFlowType? = null,

    override val fiscalYearDeviation: FiscalYearDeviation? = null,

    override val fiscalYearEnd: LocalDate? = null,

    override val scopeOfEntities: YesNoNa? = null,

    override val nfrdMandatory: YesNo? = null,

    override val euTaxonomyActivityLevelReporting: YesNo? = null,

    override val assurance: AssuranceData? = null,

    override val numberOfEmployees: BigDecimal? = null,

    override val referencedReports: Map<String, CompanyReport>? = null,
) : EuTaxonomyCommonFields, FrameworkBase
