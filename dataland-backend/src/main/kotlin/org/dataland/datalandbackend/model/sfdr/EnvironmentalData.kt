package org.dataland.datalandbackend.model.sfdr

import org.dataland.datalandbackend.model.sfdr.submodels.Biodiversity
import org.dataland.datalandbackend.model.sfdr.submodels.Emissions
import org.dataland.datalandbackend.model.sfdr.submodels.EnergyPerformance
import org.dataland.datalandbackend.model.sfdr.submodels.GreenhouseGasEmissions
import org.dataland.datalandbackend.model.sfdr.submodels.SfdrWaste
import org.dataland.datalandbackend.model.sfdr.submodels.Water

/**
 * --- API model ---
 * Impact topics for the "Environmental" impact area of the SFDR framework
 */
data class EnvironmentalData(
    val greenhouseGasEmissions: GreenhouseGasEmissions? = null,

    val energyPerformance: EnergyPerformance? = null,

    val biodiversity: Biodiversity? = null,

    val water: Water? = null,

    val waste: SfdrWaste? = null,

    val emissions: Emissions? = null,
)
