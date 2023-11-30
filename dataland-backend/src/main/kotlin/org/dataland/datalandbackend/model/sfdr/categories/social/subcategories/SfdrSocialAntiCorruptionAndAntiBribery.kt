package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.CurrencyDataPointValidation
import org.dataland.datalandbackend.validator.ExtendedNumberDataPointValidation

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery" belonging to the category "Social" of the sfdr
 * framework.
 */
data class SfdrSocialAntiCorruptionAndAntiBribery(
    @field:ExtendedNumberDataPointValidation
    val casesOfInsufficientActionAgainstBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:ExtendedNumberDataPointValidation
    val reportedConvictionsOfBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:CurrencyDataPointValidation
    val totalAmountOfReportedFinesOfBriberyAndCorruption: CurrencyDataPoint? = null,
)
