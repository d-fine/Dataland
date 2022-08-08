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
    @field:JsonProperty("Financial Services Type", required = true) val financialServicesType: FinancialServicesType?,
    @field:JsonProperty("Taxonomy Eligible Activity") val taxonomyEligibleActivity: BigDecimal? = null,
    @field:JsonProperty("Derivatives") val derivatives: BigDecimal? = null,
    @field:JsonProperty("Banks and Issuers") val banksAndIssuers: BigDecimal? = null,
    @field:JsonProperty("Investment non Nfrd") val investmentNonNfrd: BigDecimal? = null,
    @field:JsonProperty("Reporting Obligation", required = true) val reportObligation: YesNo? = null,
    @field:JsonProperty("Attestation", required = true) val attestation: AttestationOptions? = null,
    @field:JsonProperty("Trading Portfolio") val tradingPortfolio: BigDecimal? = null,
    @field:JsonProperty("Interbank Loans") val interbankLoans: BigDecimal? = null,
    @field:JsonProperty("Trading Portfolio and Interbank Loans")
    val tradingPortfolioAndInterbankLoans: BigDecimal? = null,
    @field:JsonProperty("Taxonomy Eligible non Life Insurance Activities")
    val taxonomyEligibleNonLifeInsuranceActivities: BigDecimal? = null
)
