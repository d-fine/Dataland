package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Water" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalWater(
    val emissionsToWater: DataPoint<BigDecimal>? = null,

    val waterConsumption: DataPoint<BigDecimal>? = null,

    val waterReused: DataPoint<BigDecimal>? = null,

    val waterManagementPolicy: DataPoint<YesNo>? = null,

    val waterStressAreaExposure: DataPoint<YesNo>? = null,
)
