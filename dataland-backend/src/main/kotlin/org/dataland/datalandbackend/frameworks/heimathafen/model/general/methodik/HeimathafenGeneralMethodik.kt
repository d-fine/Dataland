package org.dataland.datalandbackend.frameworks.heimathafen.model.general.methodik

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the Methodik section
 */
data class HeimathafenGeneralMethodik(
    val verstaendnisVonNachhaltigkeitAlsTeilDerBewertung: String?,
    val qualitaetssicherungsprozess: YesNo?,
)
