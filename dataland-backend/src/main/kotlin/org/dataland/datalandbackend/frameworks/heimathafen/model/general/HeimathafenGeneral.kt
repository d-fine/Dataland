package org.dataland.datalandbackend.frameworks.heimathafen.model.general

import org.dataland.datalandbackend.frameworks.heimathafen.model.general.datenanbieter.HeimathafenGeneralDatenanbieter
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.methodik.HeimathafenGeneralMethodik
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.impactmerkmale.HeimathafenGeneralImpactmerkmale
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.implementierung.HeimathafenGeneralImplementierung

/**
 * The data-model for the General section
 */
data class HeimathafenGeneral(
    val datenanbieter: HeimathafenGeneralDatenanbieter?,
    val methodik: HeimathafenGeneralMethodik?,
    val impactmerkmale: HeimathafenGeneralImpactmerkmale?,
    val implementierung: HeimathafenGeneralImplementierung?,
)
