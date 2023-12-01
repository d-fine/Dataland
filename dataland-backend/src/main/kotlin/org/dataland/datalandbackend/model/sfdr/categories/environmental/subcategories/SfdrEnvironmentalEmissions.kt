package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.DataPointMinimumValue
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Emissions" belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalEmissions(
    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val emissionsOfInorganicPollutantsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val emissionsOfAirPollutantsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val emissionsOfOzoneDepletionSubstancesInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val carbonReductionInitiatives: ExtendedDataPoint<YesNo>? = null,
)
