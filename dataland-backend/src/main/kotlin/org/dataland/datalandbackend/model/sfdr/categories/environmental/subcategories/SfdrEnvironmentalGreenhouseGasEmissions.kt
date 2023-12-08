package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Greenhouse gas emissions " belonging
 * to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalGreenhouseGasEmissions(
    @field:Valid
    val scope1GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope2GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope2GhgEmissionsLocationBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope2GhgEmissionsMarketBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope1And2GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope1And2GhgEmissionsLocationBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope1And2GhgEmissionsMarketBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope3GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val scope1And2And3GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val enterpriseValue: CurrencyDataPoint? = null,

    @field:Valid
    val totalRevenue: CurrencyDataPoint? = null,

    @field:Valid
    val carbonFootprintInTonnesPerMillionEURRevenue: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val ghgIntensityInTonnesPerMillionEURRevenue: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val fossilFuelSectorExposure: ExtendedDataPoint<YesNo>? = null,
)
