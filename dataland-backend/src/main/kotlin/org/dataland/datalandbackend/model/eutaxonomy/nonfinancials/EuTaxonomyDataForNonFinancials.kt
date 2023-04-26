package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
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
