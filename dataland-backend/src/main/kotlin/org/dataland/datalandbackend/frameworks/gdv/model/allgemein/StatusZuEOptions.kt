package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field statusZuE
 */
@Schema(
    enumAsRef = true,
)
enum class StatusZuEOptions(val value: String) {
    Offen("offen"),
    Geklaert("geklärt"),
}
