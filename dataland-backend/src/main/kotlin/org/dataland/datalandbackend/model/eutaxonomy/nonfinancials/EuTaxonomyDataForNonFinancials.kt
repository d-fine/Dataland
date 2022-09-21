package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.CompanyReport
import org.dataland.datalandbackend.model.FrameworkBase
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.dataland.datalandbackend.model.eutaxonomy.EuTaxonomyCommonFields
import java.time.LocalDate

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForNonFinancials framework
 */
@DataType("eutaxonomy-non-financials")
data class EuTaxonomyDataForNonFinancials(
    @field:JsonProperty("capex")
    val capex: EuTaxonomyDetailsPerCashFlowType? = null,

    @field:JsonProperty("opex")
    val opex: EuTaxonomyDetailsPerCashFlowType? = null,

    @field:JsonProperty("revenue")
    val revenue: EuTaxonomyDetailsPerCashFlowType? = null,

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
