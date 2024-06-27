package org.dataland.datalandbackend.frameworks.vsme.custom

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Waste Classification
 */
@Schema(
    enumAsRef = true,
)
enum class WasteClassifications {
    NonHazardous,
    Hazardous,
}
