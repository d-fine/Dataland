package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field anreizmechanismenFuerDasManagementSoziales
 */
@Schema(
    enumAsRef = true,
)
enum class AnreizmechanismenFuerDasManagementSozialesOptions {
    Nein,
    JaAufsichtsrat,
    JaGeschaeftsleitung,
    JaAufsichtsratUndGeschaeftsleitung,
}
