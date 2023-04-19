package org.dataland.datalandbackend.model.enums.lksg

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Percentage intervals for the share of temporary workers compared to the total number of employees in a company
 */
@Schema(
    enumAsRef = true,
)
enum class ShareOfTemporaryWorkers {
    Smaller10, Between10And25, Between25And50, Greater50
}
