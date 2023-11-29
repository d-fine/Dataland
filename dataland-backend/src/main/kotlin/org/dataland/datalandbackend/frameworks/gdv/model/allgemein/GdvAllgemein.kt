package org.dataland.datalandbackend.frameworks.gdv.model.allgemein

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Allgemein section
 */
data class GdvAllgemein(
    val esgZiele: YesNo?,
    val ziele: String?,
    val investitionen: String?,
    val sektorMitHohenKlimaauswirkungen: YesNo?,
)
