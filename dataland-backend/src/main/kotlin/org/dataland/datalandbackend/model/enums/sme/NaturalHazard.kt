package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the natural hazards selection
 */
@Schema(
    enumAsRef = true,
)
enum class NaturalHazard {
    Hail, Wind, Flooding, EarthQuake, Avalanche, Snow
}
