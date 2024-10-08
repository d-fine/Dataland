// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.sfdr.model.environmental.emissions

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.MinimumValue
import java.math.BigDecimal

/**
 * The data-model for the Emissions section
 */
data class SfdrEnvironmentalEmissions(
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val emissionsOfInorganicPollutantsInTonnes: ExtendedDataPoint<BigDecimal?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val emissionsOfAirPollutantsInTonnes: ExtendedDataPoint<BigDecimal?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:Valid()
    val emissionsOfOzoneDepletionSubstancesInTonnes: ExtendedDataPoint<BigDecimal?>? = null,
    @field:Valid()
    val carbonReductionInitiatives: ExtendedDataPoint<YesNo?>? = null,
)
