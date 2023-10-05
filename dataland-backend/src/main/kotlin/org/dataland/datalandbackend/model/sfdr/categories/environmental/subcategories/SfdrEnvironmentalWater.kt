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
    val emissionsToWaterInTonnes: DataPointWithUnit<BigDecimal>? = null,

    val waterConsumptionInCubicMeters: DataPointWithUnit<BigDecimal>? = null,

    val waterReusedInCubicMeters: DataPointWithUnit<BigDecimal>? = null,

    val waterManagementPolicy: DataPointOneValue<YesNo>? = null,

    val highWaterStressAreaExposure: DataPointOneValue<YesNo>? = null,
)
