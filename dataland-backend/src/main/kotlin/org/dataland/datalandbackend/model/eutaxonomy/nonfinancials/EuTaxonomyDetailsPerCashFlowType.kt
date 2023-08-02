package org.dataland.datalandbackend.model.eutaxonomy.nonfinancials

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.DataPointAbsoluteAndPercentage
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields for each cashflow type in the EuTaxonomyForNonFinancials framework
 */
data class EuTaxonomyDetailsPerCashFlowType(
    val totalAmount: DataPointOneValue<BigDecimal>? = null,

    val alignedData: DataPointAbsoluteAndPercentage<BigDecimal>? = null,

    val eligibleData: DataPointAbsoluteAndPercentage<BigDecimal>? = null,
)
