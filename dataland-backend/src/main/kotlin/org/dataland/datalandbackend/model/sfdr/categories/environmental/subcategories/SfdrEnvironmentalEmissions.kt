package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Emissions" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalEmissions(
    val inorganicPollutantsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val airPollutantsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val ozoneDepletionSubstancesInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val carbonReductionInitiatives: ExtendedDataPoint<YesNo>? = null,
)
