package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Emissions" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEmissions(
    val emissionsOfInorganicPollutantsInTonnes: DataPointWithUnit<BigDecimal>? = null,

    val emissionsOfAirPollutantsInTonnes: DataPointWithUnit<BigDecimal>? = null,

    val emissionsOfOzoneDepletionSubstancesInTonnes: DataPointWithUnit<BigDecimal>? = null,

    val carbonReductionInitiatives: DataPointOneValue<YesNo>? = null,
)
