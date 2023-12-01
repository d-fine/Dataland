package org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.sfdr.HighImpactClimateSector
import org.dataland.datalandbackend.model.sfdr.custom.SfdrHighImpactClimateSectorEnergyConsumption
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants
import org.dataland.datalandbackend.validator.DataPointMaximumValue
import org.dataland.datalandbackend.validator.DataPointMinimumValue
import java.math.BigDecimal

/**
 * --- API model ---
 * Fields of the subcategory "Energy performance" belonging to the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmentalEnergyPerformance(
    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val renewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val renewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyProductionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:DataPointMaximumValue(maximumValue = 100)
    @field:Valid
    val relativeNonRenewableEnergyProductionInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:DataPointMaximumValue(maximumValue = 100)
    @field:Valid
    val relativeNonRenewableEnergyConsumptionInPercent: ExtendedDataPoint<BigDecimal>? = null,

    @field:Schema(
        example = JsonExampleFormattingConstants.HIGH_IMPACT_CLIMATE_SECTORS_DEFAULT_VALUE,
    )
    val applicableHighImpactClimateSectors:
    Map<HighImpactClimateSector, SfdrHighImpactClimateSectorEnergyConsumption>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val totalHighImpactClimateSectorEnergyConsumptionInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionFossilFuelsInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionCrudeOilInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionNaturalGasInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionLigniteInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionCoalInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionNuclearEnergyInGWh: ExtendedDataPoint<BigDecimal>? = null,

    @field:DataPointMinimumValue(minimumValue = 0)
    @field:Valid
    val nonRenewableEnergyConsumptionOtherInGWh: ExtendedDataPoint<BigDecimal>? = null,
)
