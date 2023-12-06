package org.dataland.datalandbackend.frameworks.gdv.model.soziales.audit

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field artDesAudits
 */
@Schema(
    enumAsRef = true,
)
enum class ArtDesAuditsOptions {
    InterneAnhoerung,
    PruefungDurchDritte,
    SowohlInternAlsAuchVonDrittanbietern,
}
