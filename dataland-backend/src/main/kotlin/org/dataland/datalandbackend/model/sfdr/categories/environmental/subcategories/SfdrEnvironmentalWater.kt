package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPointOneValue
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Water" belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalWater(
    val emissionsToWater: DataPointWithUnit<BigDecimal>? = null,

    val waterConsumption: DataPointWithUnit<BigDecimal>? = null,

    val waterReused: DataPointWithUnit<BigDecimal>? = null,

    val waterManagementPolicy: DataPointOneValue<YesNo>? = null,

    val waterStressAreaExposure: DataPointOneValue<YesNo>? = null,
)
