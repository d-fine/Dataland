package org.dataland.datalandbackend.frameworks.heimathafen.model.general

import org.dataland.datalandbackend.frameworks.heimathafen.model.general.datenanbieter.HeimathafenGeneralDatenanbieter
import org.dataland.datalandbackend.frameworks.heimathafen.model.general.methodik.HeimathafenGeneralMethodik

/**
 * The data-model for the General section
 */
data class HeimathafenGeneral(
    val datenanbieter: HeimathafenGeneralDatenanbieter?,
    val methodik: HeimathafenGeneralMethodik?,
)
