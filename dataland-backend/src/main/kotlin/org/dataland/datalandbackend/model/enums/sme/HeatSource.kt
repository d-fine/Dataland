package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible types for the source of building heating in the SME framework
 */
@Schema(
    enumAsRef = true
)
enum class HeatSource {
    Oil, Gas, Electricity, DistrictHeat
}
