package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field statusE
 */
@Schema(
enumAsRef = true,
)
enum class StatusEOptions {
    Offen,
    Geklaert,
}
