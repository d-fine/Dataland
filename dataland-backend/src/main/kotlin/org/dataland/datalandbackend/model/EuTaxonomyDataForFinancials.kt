package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.annotations.DataType
import org.dataland.datalandbackend.model.enums.AttestationOptions
import org.dataland.datalandbackend.model.enums.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the questionnaire for EU-Taxonomy data for financial companies
 */
@DataType
data class EuTaxonomyDataForFinancials(
    @field:JsonProperty("") val taxonomyEligibleActivity: BigDecimal? = null,
    @field:JsonProperty("") val derivatives: BigDecimal? = null,
    @field:JsonProperty("") val banksAndIssuers: BigDecimal? = null,
    @field:JsonProperty("") val investmentNonNfrd: BigDecimal? = null,
    @field:JsonProperty("Reporting Obligation", required = true) val reportObligation: YesNo? = null,
    @field:JsonProperty("Attestation", required = true) val attestation: AttestationOptions? = null,
    @field:JsonProperty("") val tradingPortfolio: BigDecimal? = null,
    @field:JsonProperty("") val interbankLoans: BigDecimal? = null,
    @field:JsonProperty("") val tradingPortfolioAndInterbankLoans: BigDecimal? = null,
    @field:JsonProperty("") val taxonomyEligibleNonLifeInsuranceActivities: BigDecimal? = null
)
