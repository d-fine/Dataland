package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Production and use of persistent organic pollutants
 * (POPs Convention)"
 */
data class LksgProductionAndUseOfPersistentOrganicPollutantsPopsConvention(
        val persistentOrganicPollutantsProductionAndUse: BaseDataPoint<YesNo>?,

        val persistentOrganicPollutantsUsed: BaseDataPoint<String>?,

        val persistentOrganicPollutantsProductionAndUseRiskOfExposure: BaseDataPoint<YesNo>?,

        val persistentOrganicPollutantsProductionAndUseRiskOfDisposal: BaseDataPoint<YesNo>?,

        val legalRestrictedWasteProcesses: BaseDataPoint<YesNo>?,
)
