package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery" belonging to the category "Social" of the sfdr
 * framework.
 */
data class SfdrSocialAntiCorruptionAndAntiBribery(
    val casesOfInsufficientActionAgainstBriberyAndCorruption: ExtendedDataPoint<BigDecimal>? = null,

    val reportedConvictionsOfBriberyAndCorruption: ExtendedDataPoint<BigDecimal>? = null,

    val totalAmountOfReportedFinesOfBriberyAndCorruption: CurrencyDataPoint = null,
)
