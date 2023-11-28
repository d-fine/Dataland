package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field statusG
 */
@Schema(
enumAsRef = true,
)
enum class StatusGOptions {
    Offen,
    Geklaert,
}
