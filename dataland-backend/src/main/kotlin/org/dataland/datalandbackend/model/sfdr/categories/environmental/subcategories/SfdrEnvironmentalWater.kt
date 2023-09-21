package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Water" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalWater(
    val emissionsToWater: ExtendedDataPoint<BigDecimal>? = null,

    val waterConsumption: ExtendedDataPoint<BigDecimal>? = null,

    val waterReused: ExtendedDataPoint<BigDecimal>? = null,

    val waterManagementPolicy: ExtendedDataPoint<YesNo>? = null,

    val waterStressAreaExposure: ExtendedDataPoint<YesNo>? = null,
)
