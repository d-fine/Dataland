package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the energy source for heating and hot water
 */
@Schema(
    enumAsRef = true,
)
enum class PercentRangeForInvestmentsInEnergyEfficiency(val numberString: String) {
    LessThan1("< 1%"),
    Between1And5("1-5%"),
    Between5And10("5-10%"),
    Between10And15("10-15%"),
    Between15And20("15-20%"),
    Between20And25("20-25%"),
    GreaterThan25("> 25%"),
}
