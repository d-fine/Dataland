package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the industry in the SME framework
 */
@Schema(
    enumAsRef = true
)
enum class Industry {
    ConstructionIndustry, ManufacturingIndustry, Trade
}
