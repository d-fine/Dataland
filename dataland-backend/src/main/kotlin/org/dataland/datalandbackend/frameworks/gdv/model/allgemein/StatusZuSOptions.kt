package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field statusZuS
 */
@Schema(
    enumAsRef = true,
)
enum class StatusZuSOptions {
    Offen,
    Geklaert,
}
