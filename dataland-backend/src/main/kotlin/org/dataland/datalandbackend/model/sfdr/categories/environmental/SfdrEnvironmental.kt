package org.dataland.datalandbackend.model.sfdr.categories.environmental

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalBiodiversity
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalEmissions
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalEnergyPerformance
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalGreenhouseGasEmissions
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalWaste
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalWater

/**
 * --- API model ---
 * Fields of the category "Environmental" of the sfdr framework.
*/
data class SfdrEnvironmental(
    @field:Valid
    val greenhouseGasEmissions: SfdrEnvironmentalGreenhouseGasEmissions? = null,

    @field:Valid
    val energyPerformance: SfdrEnvironmentalEnergyPerformance? = null,

    val biodiversity: SfdrEnvironmentalBiodiversity? = null,

    @field:Valid
    val water: SfdrEnvironmentalWater? = null,

    @field:Valid
    val waste: SfdrEnvironmentalWaste? = null,

    @field:Valid
    val emissions: SfdrEnvironmentalEmissions? = null,
)
