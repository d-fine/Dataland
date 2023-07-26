package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the energy source for heating and hot water
 */
@Schema(
    enumAsRef = true,
)
enum class PercentRangeForEnergyConsumptionCoveredByOwnRenewablePower(val numberString: String) {
    LessThan25("< 25%"),
    Between25And50("25-50%"),
    Between50And75("50-75%"),
    GreaterThan75("> 75%"),
}
