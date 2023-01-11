package org.dataland.datalandbackend.model.sfdr

/**
 * --- API model ---
 * Impact topics for the "Environmental" impact area of the SFDR framework
 */
data class EnvironmentalData(
    val greenhouseGasEmissions: GreenhouseGasEmissions? = null,

    val energyPerformance: EnergyPerformance? = null,

    val biodiversity: Biodiversity? = null,

    val water: Water? = null,

    val waste: Waste? = null,

    val emissions: Emissions? = null,
)
