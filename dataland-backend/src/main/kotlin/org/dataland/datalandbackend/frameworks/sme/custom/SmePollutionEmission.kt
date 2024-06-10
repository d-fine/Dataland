package org.dataland.datalandbackend.frameworks.sme.custom

import org.dataland.datalandbackend.model.enums.sme.ReleaseMedium
import java.math.BigDecimal

/**
 * --- API model ---
 * Pollution emission class for vsme framework
 */
data class SmePollutionEmission(
    val pollutionType: String?,

    val emissionInKilograms: BigDecimal? = null,

    val releaseMedium: ReleaseMedium? = null,
// TODO check if fields are sensible nullable
    // TODO check nullable of fields
)
