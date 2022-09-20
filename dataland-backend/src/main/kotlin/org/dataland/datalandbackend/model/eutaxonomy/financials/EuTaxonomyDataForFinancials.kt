package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.dataland.datalandbackend.model.DataPoint
import java.util.EnumSet

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

    @field:JsonProperty("assurance")
    val assurance: AssuranceData? = null,

    @field:JsonProperty("greenAssetRatio")
    val greenAssetRatio: DataPoint? = null,

    @field:JsonProperty("reportingObligation")
    val reportingObligation: YesNo? = null,

    @field:JsonProperty("activityLevelReporting")
    val activityLevelReporting: YesNo? = null
)
