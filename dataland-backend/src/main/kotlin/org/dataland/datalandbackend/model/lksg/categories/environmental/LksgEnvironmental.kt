package org.dataland.datalandbackend.model.lksg.categories.environmental

import org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories.LksgEnvironmentalUseOfMercuryMercuryWasteMinamataConvention
import org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories.LksgEnvironmentalProductionAndUseOfPersistentOrganicPollutantsPopsConvention
import org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories.LksgEnvironmentalExportImportOfHazardousWasteBaselConvention

/**
 * --- API model ---
 * Fields of the category "Environmental" of the lksg framework.
*/
data class LksgEnvironmental(
      val useOfMercuryMercuryWasteMinamataConvention: LksgEnvironmentalUseOfMercuryMercuryWasteMinamataConvention? = null,

      val productionAndUseOfPersistentOrganicPollutantsPopsConvention: LksgEnvironmentalProductionAndUseOfPersistentOrganicPollutantsPopsConvention? = null,

      val exportImportOfHazardousWasteBaselConvention: LksgEnvironmentalExportImportOfHazardousWasteBaselConvention? = null,
)
