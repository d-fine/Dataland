package org.dataland.datalandbackend.model.enums.sfdr

import io.swagger.v3.oas.annotations.media.Schema

/**
 * High impact climate sectors identified via their corresponding Nace codes
 */
@Schema(
    enumAsRef = true,
)
enum class HighImpactClimateSector {
    NaceCodeAInGWh,
    NaceCodeBInGWh,
    NaceCodeCInGWh,
    NaceCodeDInGWh,
    NaceCodeEInGWh,
    NaceCodeFInGWh,
    NaceCodeGInGWh,
    NaceCodeHInGWh,
    NaceCodeLInGWh,
}
