package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the energy source for heating and hot water
 */
@Schema(
    enumAsRef = true,
)
enum class EnergySourceForHeatingAndHotWater {
    Oil, Gas, Electric, DistrictHeating
}
