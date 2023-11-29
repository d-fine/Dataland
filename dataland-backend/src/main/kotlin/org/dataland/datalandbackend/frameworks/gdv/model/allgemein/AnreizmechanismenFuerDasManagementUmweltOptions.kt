package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum class for the field anreizmechanismenFuerDasManagementUmwelt
 */
@Schema(
    enumAsRef = true,
)
enum class AnreizmechanismenFuerDasManagementUmweltOptions(val value: String) {
    Nein("Nein"),
    JaAufsichtsrat("Ja, Aufsichtsrat"),
    JaGeschaeftsleitung("Ja, Geschäftsleitung"),
    JaAufsichtsratUndGeschaeftsleitung("Ja, Aufsichtsrat und Geschäftsleitung"),
}
