package org.dataland.datalandbackend.frameworks.nuclearandgas.model.custom

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Custom Component for the EU-Taxonomy Nuclear and Gas Template Non-Eligible
 */
data class NuclearAndGasnonEligible(
    @field:Valid()
    val TaxonomyNonEligibleShareNAndG426: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val TaxonomyNonEligibleShareNAndG427: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val TaxonomyNonEligibleShareNAndG428: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val TaxonomyNonEligibleShareNAndG429: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val TaxonomyNonEligibleShareNAndG430: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val TaxonomyNonEligibleShareNAndG431: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val TaxonomyNonEligibleShareOtherActivities: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val TaxonomyNonEligibleShare: ExtendedDataPoint<BigDecimal?>? = null,
)
