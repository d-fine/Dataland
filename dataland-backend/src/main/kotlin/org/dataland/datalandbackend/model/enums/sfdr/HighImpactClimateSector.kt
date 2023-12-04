package org.dataland.datalandbackend.model.enums.sfdr

import io.swagger.v3.oas.annotations.media.Schema

/**
 * High impact climate sectors identified via their corresponding Nace codes
 */
@Schema(
    enumAsRef = true,
)
enum class HighImpactClimateSector {
    NaceCodeA,
    NaceCodeB,
    NaceCodeC,
    NaceCodeD,
    NaceCodeE,
    NaceCodeF,
    NaceCodeG,
    NaceCodeH,
    NaceCodeL,
}
