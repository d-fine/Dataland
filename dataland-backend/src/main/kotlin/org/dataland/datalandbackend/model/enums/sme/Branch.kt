package org.dataland.datalandbackend.model.enums.sme

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible values for the branch in the SME framework
 */
@Schema(
    enumAsRef = true
)
enum class Branch {
    Baugewerbe, VerarbeitendesGewerbe, Handel
}
