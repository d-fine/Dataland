package org.dataland.datalandbackend.frameworks.gdv.model.umwelt.taxonomie

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field taxonomieBerichterstattung
 */
@Schema(
    enumAsRef = true,
)
enum class TaxonomieBerichterstattungOptions(val value: String) {
    Nfrd("NFRD"),
    Csrd("CSRD"),
}
