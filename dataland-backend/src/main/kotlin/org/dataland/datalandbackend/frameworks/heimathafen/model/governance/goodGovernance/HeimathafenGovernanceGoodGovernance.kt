package org.dataland.datalandbackend.frameworks.heimathafen.model.governance.goodGovernance

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * The data-model for the GoodGovernance section
 */
data class HeimathafenGovernanceGoodGovernance(
    val methodikDerGutenRegierungsfuehrung: YesNo?,
    val wennMethodikDerGutenRegierungsfuehrungNeinBitteBegruenden: String?,
    val definitionVonGuterRegierungsfuehrung: String?,
    val listeDerKpisFuerGuteUnternehmensfuehrung: String?,
    val verwendeteQuellenFuerMethodikDerGutenRegierungsfuehrung: String?,
    val beruecksichtigungDesUngc: YesNo?,
    val wennBeruecksichtigungDesUngcNeinBitteBegruenden: String?,
    val beruecksichtigungDerUngcBeschreibung: String?,
    val verwendeteQuellenFuerBeruecksichtigungDesUngc: String?,
)
