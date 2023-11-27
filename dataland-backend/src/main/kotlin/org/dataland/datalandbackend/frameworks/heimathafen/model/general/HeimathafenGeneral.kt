package org.dataland.datalandbackend.frameworks.heimathafen.model.general

import org.dataland.datalandbackend.frameworks.heimathafen.model.general.datenanbieter.HeimathafenGeneralDatenanbieter

/**
 * The data-model for the General section
 */
data class HeimathafenGeneral(
    val datenanbieter: HeimathafenGeneralDatenanbieter?,
)
