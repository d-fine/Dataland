// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.heimathafen.model.environmental.paiBiologischeVielfalt

import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the PaiBiologischeVielfalt section
 */
data class HeimathafenEnvironmentalPaiBiologischeVielfalt(
    val paisBiologischeVielfalt: YesNo? = null,
    val wennNeinBitteBegruenden: String? = null,
    val verwendeteSchluesselzahlen: String? = null,
    val datenerfassung: String? = null,
    val datenPlausibilitaetspruefung: String? = null,
    @field:Valid()
    val datenquellen: List<BaseDataPoint<String>>? = null,
)
