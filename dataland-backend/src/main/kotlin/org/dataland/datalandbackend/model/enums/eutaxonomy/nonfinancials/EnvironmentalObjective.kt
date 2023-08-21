package org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum that holds the different environmental objectives related to EU taxonomy for non financial companies
 */
@Schema(
    enumAsRef = true,
)
enum class EnvironmentalObjective(val value: String) {
    ClimateMitigation("mitigation"),
    ClimateAdaptation("adaptation"),
    Water("water"),
    CircularEconomy("circular"),
    PollutionPrevention("pollution"),
    Biodiversity("biodiversity"),
}
