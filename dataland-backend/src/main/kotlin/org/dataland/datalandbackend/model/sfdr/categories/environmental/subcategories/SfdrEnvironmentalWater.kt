package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.ExtendedNumberDataPointValidation
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Water" belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalWater(
    @field:ExtendedNumberDataPointValidation
    @field:Valid
    val emissionsToWaterInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:ExtendedNumberDataPointValidation
    @field:Valid
    val waterConsumptionInCubicMeters: ExtendedDataPoint<BigDecimal>? = null,

    @field:ExtendedNumberDataPointValidation
    @field:Valid
    val waterReusedInCubicMeters: ExtendedDataPoint<BigDecimal>? = null,

    @field:ExtendedNumberDataPointValidation
    @field:Valid
    val relativeWaterUsageInCubicMetersPerMillionEURRevenue: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val waterManagementPolicy: ExtendedDataPoint<YesNo>? = null,

    @field:Valid
    val highWaterStressAreaExposure: ExtendedDataPoint<YesNo>? = null,
)
