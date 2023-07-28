package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.DataPointWithUnit
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Greenhouse gas emissions " belonging to the category "Environmental" of the
 * sfdr framework.
 */
data class SfdrEnvironmentalGreenhouseGasEmissions(
    val scope1: DataPointWithUnit<BigDecimal>? = null,

    val scope2: DataPointWithUnit<BigDecimal>? = null,

    val scope3: DataPointWithUnit<BigDecimal>? = null,

    val enterpriseValue: DataPointWithUnit<BigDecimal>? = null,

    val totalRevenue: DataPointWithUnit<BigDecimal>? = null,

    val fossilFuelSectorExposure: DataPoint<YesNo>? = null,
)
