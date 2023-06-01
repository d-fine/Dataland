package org.dataland.datalandbackend.model.sfdr.categories.environmental

import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrBiodiversity
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEmissions
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrEnergyPerformance
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrGreenhouseGasEmissions
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrWaste
import org.dataland.datalandbackend.model.sfdr.categories.environmental.subcategories.SfdrWater

/**
 * --- API model ---
 * Impact topics for the "Environmental" impact area of the SFDR framework
 */
data class SfdrEnvironmental(
    val greenhouseGasEmissions: SfdrGreenhouseGasEmissions?,

    val energyPerformance: SfdrEnergyPerformance?,

    val biodiversity: SfdrBiodiversity?,

    val water: SfdrWater?,

    val waste: SfdrWaste?,

    val emissions: SfdrEmissions?,
)
