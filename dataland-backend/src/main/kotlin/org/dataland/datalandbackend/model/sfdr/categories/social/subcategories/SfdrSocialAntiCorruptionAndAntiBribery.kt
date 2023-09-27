package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.CurrencyDataPoint
import org.dataland.datalandbackend.model.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery" belonging to the category "Social" of the sfdr
 * framework.
 */
data class SfdrSocialAntiCorruptionAndAntiBribery(
    val reportedCasesOfBriberyCorruption: ExtendedDataPoint<BigDecimal>? = null,

    val reportedConvictionsOfBriberyCorruption: ExtendedDataPoint<BigDecimal>? = null,

    val reportedFinesOfBriberyCorruption: CurrencyDataPoint? = null,
)
