package org.dataland.datalandbackend.model.eutaxonomy.financials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.eutaxonomy.AttestationOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import org.dataland.datalandbackend.model.enums.eutaxonomy.financials.FinancialServicesType
import java.util.EnumSet

/**
 * --- API model ---
 * Fields of the questionnaire for the EuTaxonomyForFinancials framework
 */
@DataType
data class EuTaxonomyDataForFinancials(
    @field:JsonProperty("financialServicesTypes", required = true)
    val financialServicesTypes: EnumSet<FinancialServicesType>?,

    @field:JsonProperty("eligibilityKpis")
    val eligibilityKpis: Map<FinancialServicesType, EligibilityKpis>? = null,

    @field:JsonProperty("creditInstitutionKpis")
    val creditInstitutionKpis: CreditInstitutionKpis? = null,

    @field:JsonProperty("insuranceKpis")
    val insuranceKpis: InsuranceKpis? = null,

    @field:JsonProperty("attestation", required = true)
    val attestation: AttestationOptions? = null,

    @field:JsonProperty("reportingObligation", required = true)
    val reportingObligation: YesNo? = null,
)
