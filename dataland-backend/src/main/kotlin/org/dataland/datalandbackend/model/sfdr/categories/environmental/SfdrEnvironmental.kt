package org.dataland.datalandbackend.model.sfdr.categories.environmental

import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalGreenhouseGasEmissions
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalEnergyPerformance
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalBiodiversity
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalWater
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalWaste
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnvironmentalEmissions

/**
 * --- API model ---
 * Fields of the category "Environmental" of the sfdr framework.
 */
data class SfdrEnvironmental(
    val greenhouseGasEmissions: SfdrEnvironmentalGreenhouseGasEmissions? = null,

    val energyPerformance: SfdrEnvironmentalEnergyPerformance? = null,

    val biodiversity: SfdrEnvironmentalBiodiversity? = null,

    val water: SfdrEnvironmentalWater? = null,

    val waste: SfdrEnvironmentalWaste? = null,

    val emissions: SfdrEnvironmentalEmissions? = null,
)
