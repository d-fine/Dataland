package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Greenhouse gas emissions " belonging to the category "Environmental" of the sfdr
 * framework.
 */
data class SfdrEnvironmentalGreenhouseGasEmissions(
    val scope1: ExtendedDataPoint<BigDecimal>? = null,

    val scope2: ExtendedDataPoint<BigDecimal>? = null,

    val scope3: ExtendedDataPoint<BigDecimal>? = null,

    val enterpriseValue: ExtendedDataPoint<BigDecimal>? = null,

    val totalRevenue: ExtendedDataPoint<BigDecimal>? = null,

    val fossilFuelSectorExposure: ExtendedDataPoint<YesNo>? = null,
)
