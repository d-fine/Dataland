package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.DataPointMinimumValue
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Greenhouse gas emissions "
 * belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalGreenhouseGasEmissions(
    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope1GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope2GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope2GhgEmissionsLocationBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope2GhgEmissionsMarketBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope1And2GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope1And2GhgEmissionsLocationBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope1And2GhgEmissionsMarketBasedInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope3GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val scope1And2And3GhgEmissionsInTonnes: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val enterpriseValue: CurrencyDataPoint? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val totalRevenue: CurrencyDataPoint? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val carbonFootprintInTonnesPerMillionEURRevenue: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    @field:DataPointMinimumValue(minimumValue = -10000000000)
    val ghgIntensityInTonnesPerMillionEURRevenue: ExtendedDataPoint<BigDecimal>? = null,

    @field:Valid
    val fossilFuelSectorExposure: ExtendedDataPoint<YesNo>? = null,
)
