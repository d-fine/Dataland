package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.DataPoint
import java.math.BigDecimal
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Greenhouse gas emissions " belonging to the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmentalGreenhouseGasEmissions(
    val scope1: DataPoint<BigDecimal>? = null,

    val scope2: DataPoint<BigDecimal>? = null,

    val scope3: DataPoint<BigDecimal>? = null,

    val enterpriseValue: DataPoint<BigDecimal>? = null,

    val totalRevenue: DataPoint<BigDecimal>? = null,

    val fossilFuelSectorExposure: DataPoint<YesNo>? = null,
)
