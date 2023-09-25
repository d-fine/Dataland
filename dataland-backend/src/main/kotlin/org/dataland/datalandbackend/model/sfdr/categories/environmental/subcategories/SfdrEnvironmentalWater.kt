package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Water" belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalWater(
    val emissionsToWaterInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val waterConsumptionInCubicMeters: ExtendedDataPoint<BigDecimal>? = null,

    val waterReusedInCubicMeters: ExtendedDataPoint<BigDecimal>? = null,

    val waterManagementPolicy: ExtendedDataPoint<YesNo>? = null,

    val waterStressAreaExposure: ExtendedDataPoint<YesNo>? = null,
)
