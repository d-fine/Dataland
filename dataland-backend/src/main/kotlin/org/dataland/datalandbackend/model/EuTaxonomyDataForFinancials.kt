package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.eutaxonomy.AttestationOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.FinancialServicesType
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the questionnaire for EU-Taxonomy data for financial companies
 */
@DataType
data class EuTaxonomyDataForFinancials(
    @field:JsonProperty("financialServicesType", required = true)
    val financialServicesType: FinancialServicesType?,

    @field:JsonProperty("eligibilityKpis")
    val eligibilityKpis: EligibilityKpis? = null,

    @field:JsonProperty("creditInstitutionKpis")
    val creditInstitutionKpis: CreditInstitutionKpis? = null,

    @field:JsonProperty("insuranceKpis")
    val insuranceKpis: InsuranceKpis? = null,

    @field:JsonProperty("attestation", required = true)
    val attestation: AttestationOptions? = null,

    @field:JsonProperty("reportingObligation", required = true)
    val reportingObligation: YesNo? = null,
) {
    /**
     * KPIs for credit institutions for the EuTaxonomyForFinancials Framework
     */
    data class CreditInstitutionKpis(
        @field:JsonProperty("tradingPortfolio")
        val tradingPortfolio: BigDecimal? = null,

        @field:JsonProperty("interbankLoans")
        val interbankLoans: BigDecimal? = null,

        @field:JsonProperty("tradingPortfolioAndInterbankLoans")
        val tradingPortfolioAndInterbankLoans: BigDecimal? = null,
    )
    /**
     * KPIs for Insurance / Reinsurance companies for the EuTaxonomyForFinancials Framework
     */
    data class InsuranceKpis(
        @field:JsonProperty("taxonomyEligibleNonLifeInsuranceActivities")
        val taxonomyEligibleNonLifeInsuranceActivities: BigDecimal? = null,
    )
    /**
     * KPIs for all companies for the EuTaxonomyForFinancials Framework
     */
    data class EligibilityKpis(
        @field:JsonProperty("taxonomyEligibleActivity")
        val taxonomyEligibleActivity: BigDecimal? = null,

        @field:JsonProperty("derivatives")
        val derivatives: BigDecimal? = null,

        @field:JsonProperty("banksAndIssuers")
        val banksAndIssuers: BigDecimal? = null,

        @field:JsonProperty("investmentNonNfrd")
        val investmentNonNfrd: BigDecimal? = null,
    )
}
