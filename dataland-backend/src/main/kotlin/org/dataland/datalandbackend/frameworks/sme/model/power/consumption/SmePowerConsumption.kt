// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.sme.model.power.consumption

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import java.math.BigDecimal

/**
 * The data-model for the Consumption section
 */
data class SmePowerConsumption(
    @field:Valid()
    val powerConsumptionInMwh: ExtendedDataPoint<BigDecimal?>? = null,

    val powerFromRenewableSources: YesNo? = null,

    val energyConsumptionHeatingAndHotWaterInMwh: BigDecimal? = null,

    val primaryEnergySourceForHeatingAndHotWater:
    SmePowerConsumptionPrimaryEnergySourceForHeatingAndHotWaterOptions? = null,

    val energyConsumptionCoveredByOwnRenewablePowerGeneration:
    SmePowerConsumptionEnergyConsumptionCoveredByOwnRenewablePowerGenerationOptions? = null,

)