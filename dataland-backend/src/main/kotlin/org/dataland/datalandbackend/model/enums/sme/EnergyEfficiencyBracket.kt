package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the energy efficiency brackets of a company in the SME framework
 */
@Schema(
    enumAsRef = true,
)
enum class EnergyEfficiencyBracket {
    LessThanOnePercent,
    OneToFivePercent,
    FiveToTenPercent,
    TenToFifteenPercent,
    FifteenToTwentyPercent,
    TwentyToTwentyfivePercent,
    MoreThanTwentyfivePercent,
}
