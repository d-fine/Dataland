package org.dataland.datalandbackend.model.sfdr.categories.social.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.validator.DataPointMinimumValue

/**
 * --- API model ---
 * Fields of the subcategory "Anti-corruption and anti-bribery"
 * belonging to the category "Social" of the sfdr framework.
*/
data class SfdrSocialAntiCorruptionAndAntiBribery(
    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val casesOfInsufficientActionAgainstBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val reportedConvictionsOfBriberyAndCorruption: ExtendedDataPoint<Long>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val totalAmountOfReportedFinesOfBriberyAndCorruption: CurrencyDataPoint? = null,
)
