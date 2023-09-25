package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import org.dataland.datalandbackend.model.CurrencyDataPoint
import org.dataland.datalandbackend.model.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Greenhouse gas emissions " belonging to the category "Environmental" of the sfdr
 * framework.
 */
data class SfdrEnvironmentalGreenhouseGasEmissions(
    val scope1InTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val scope2InTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val scope3InTonnes: ExtendedDataPoint<BigDecimal>? = null,

    val enterpriseValue: CurrencyDataPoint? = null,

    val totalRevenue: CurrencyDataPoint? = null,

    val fossilFuelSectorExposure: ExtendedDataPoint<YesNo>? = null,
)
