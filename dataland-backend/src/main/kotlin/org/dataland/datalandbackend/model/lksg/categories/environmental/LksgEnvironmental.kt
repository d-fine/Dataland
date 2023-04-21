package org.dataland.datalandbackend.model.lksg.categories.environmental

import org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories.LksgExportImportOfHazardousWasteBaselConvention
import org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories.LksgProductionAndUseOfPersistentOrganicPollutantsPopsConvention
import org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories.LksgUseOfMercuryMercuryWasteMinamataConvention

/**
 * --- API model ---
 * Impact topics of the LKSG questionnaire's impact area "Environmental"
 */
data class LksgEnvironmental(
    val useOfMercuryMercuryWasteMinamataConvention: LksgUseOfMercuryMercuryWasteMinamataConvention?,

    val productionAndUseOfPersistentOrganicPollutantsPopsConvention:
    LksgProductionAndUseOfPersistentOrganicPollutantsPopsConvention?,

    val exportImportOfHazardousWasteBaselConvention: LksgExportImportOfHazardousWasteBaselConvention?,
)
