package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum that holds different adjointness of areas
*/
@Schema(
    enumAsRef = true,
)
enum class AreaAdjointness { In, Near }
