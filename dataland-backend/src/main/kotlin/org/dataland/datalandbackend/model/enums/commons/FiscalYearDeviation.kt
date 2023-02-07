package org.dataland.datalandbackend.model.enums.commons

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the fiscal year field in the SFDR framework
 */
@Schema(
    enumAsRef = true,
)
enum class FiscalYearDeviation {
    Deviation, NoDeviation
}
