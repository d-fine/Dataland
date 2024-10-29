package org.dataland.datalandbackend.frameworks.nuclearandgas.custom

import java.math.BigDecimal

/**
 * --- API model ---
 * Custom Component for the EU-Taxonomy Nuclear and Gas Template Non-Eligible
 */
data class NuclearAndGasNonEligible(
    val taxonomyNonEligibleShareNAndG426: BigDecimal? = null,
    val taxonomyNonEligibleShareNAndG427: BigDecimal? = null,
    val taxonomyNonEligibleShareNAndG428: BigDecimal? = null,
    val taxonomyNonEligibleShareNAndG429: BigDecimal? = null,
    val taxonomyNonEligibleShareNAndG430: BigDecimal? = null,
    val taxonomyNonEligibleShareNAndG431: BigDecimal? = null,
    val taxonomyNonEligibleShareOtherActivities: BigDecimal? = null,
    val taxonomyNonEligibleShare: BigDecimal? = null,
)
