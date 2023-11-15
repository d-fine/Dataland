package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Emissions" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEmissions(
    val emissionsOfInorganicPollutantsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val emissionsOfAirPollutantsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val emissionsOfOzoneDepletionSubstancesInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val carbonReductionInitiatives: ExtendedDataPoint<YesNo>? = null,
)
