package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field frequenzDerBerichterstattung
 */
@Schema(
enumAsRef = true,
)
enum class FrequenzDerBerichterstattungOptions {
    Jaehrlich,
    Halbjaehrlich,
    Vierteljaehrlich,
    Monatlich,
}
