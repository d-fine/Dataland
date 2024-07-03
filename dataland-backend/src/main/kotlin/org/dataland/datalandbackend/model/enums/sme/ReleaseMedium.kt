package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum that holds different release medium for pollution
*/
@Schema(
    enumAsRef = true,
)
enum class ReleaseMedium { Air, Soil, Water }
