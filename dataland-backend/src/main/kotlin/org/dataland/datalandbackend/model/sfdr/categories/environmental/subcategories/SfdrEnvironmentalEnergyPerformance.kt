package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.sfdr.HighImpactClimateSector
import org.dataland.datalandbackend.model.sfdr.custom.SfdrHighImpactClimateSectorEnergyConsumption
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import org.dataland.datalandbackend.validator.NonNegativeExtendedDataPoint
import org.dataland.datalandbackend.validator.PercentageDataPointValidation
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalEnergyPerformance(
    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val renewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val renewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:PercentageDataPointValidation
    @field:Valid
    val relativeNonRenewableEnergyProductionInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:PercentageDataPointValidation
    @field:Valid
    val relativeNonRenewableEnergyConsumptionInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:Schema(
        implementation = Map::class,
        example = JsonExampleFormattingConstants.HIGH_IMPACT_CLIMATE_SECTORS_DEFAULT_VALUE,
    )
    val applicableHighImpactClimateSectors: Map<HighImpactClimateSector,
        SfdrHighImpactClimateSectorEnergyConsumption,>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val totalHighImpactClimateSectorEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionFossilFuelsInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionCrudeOilInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionNaturalGasInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionLigniteInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionCoalInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionNuclearEnergyInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:NonNegativeExtendedDataPoint
    @field:Valid
    val nonRenewableEnergyConsumptionOtherInGWh: ExtendedDataPoint<BigDecimal>? = null,
)
