package org.dataland.datalandbackend.model.enums.lksg

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Type of market a company's business operates on
 */
@Schema(
    enumAsRef = true,
)
enum class NationalOrInternationalMarket {
    National, International, Both
}
