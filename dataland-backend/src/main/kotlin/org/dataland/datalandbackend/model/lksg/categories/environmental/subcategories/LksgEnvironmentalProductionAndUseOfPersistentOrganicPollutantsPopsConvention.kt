package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Production and use of persistent organic pollutants (POPs Convention)" belonging to the
 * category "Environmental" of the lksg framework.
*/
data class LksgEnvironmentalProductionAndUseOfPersistentOrganicPollutantsPopsConvention(
    val persistentOrganicPollutantsProductionAndUse: YesNo? = null,

    val persistentOrganicPollutantsUsed: String? = null,

    val persistentOrganicPollutantsProductionAndUseRiskOfExposure: YesNo? = null,

    val persistentOrganicPollutantsProductionAndUseRiskOfDisposal: YesNo? = null,

    val legalRestrictedWasteProcesses: YesNo? = null,
)
