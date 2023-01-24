package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.model.sfdr.submodels.SfdrBiodiversity
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrEmissions
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrEnergyPerformance
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrGreenhouseGasEmissions
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrWaste
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrWater

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
