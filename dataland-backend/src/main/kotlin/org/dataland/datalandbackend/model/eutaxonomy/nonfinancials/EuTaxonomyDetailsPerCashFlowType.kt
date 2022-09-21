package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
    @field:JsonProperty("totalAmount")
    val totalAmount: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("alignedPercentage")
    val alignedPercentage: DataPoint<BigDecimal>? = null,

    @field:JsonProperty("eligiblePercentage")
    val eligiblePercentage: DataPoint<BigDecimal>? = null
)
