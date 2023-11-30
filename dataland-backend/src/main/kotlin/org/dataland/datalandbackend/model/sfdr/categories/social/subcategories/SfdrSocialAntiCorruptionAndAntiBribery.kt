package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.CurrencyDataPointValidation
import org.dataland.datalandbackend.validator.ExtendedNumberDataPointValidation

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery" belonging
 * to the category "Social" of the sfdr framework.
*/
data class SfdrSocialAntiCorruptionAndAntiBribery(
    @field:ExtendedNumberDataPointValidation
    @field:Valid
    val casesOfInsufficientActionAgainstBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:ExtendedNumberDataPointValidation
    @field:Valid
    val reportedConvictionsOfBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:CurrencyDataPointValidation
    @field:Valid
    val totalAmountOfReportedFinesOfBriberyAndCorruption: CurrencyDataPoint? = null,
)
