package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.LongNonNegativeDataPoint
import org.dataland.datalandbackend.validator.NonNegativeCurrencyDataPoint

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery" belonging to the category "Social" of the sfdr
 * framework.
 */
data class SfdrSocialAntiCorruptionAndAntiBribery(
    @field:LongNonNegativeDataPoint
    val casesOfInsufficientActionAgainstBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:LongNonNegativeDataPoint
    val reportedConvictionsOfBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:NonNegativeCurrencyDataPoint
    val totalAmountOfReportedFinesOfBriberyAndCorruption: CurrencyDataPoint? = null,
)
