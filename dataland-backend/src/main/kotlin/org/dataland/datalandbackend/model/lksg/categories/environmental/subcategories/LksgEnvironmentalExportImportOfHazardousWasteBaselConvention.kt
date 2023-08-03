package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the subcategory "Export/import of hazardous waste (Basel Convention)"
 * belonging to the category "Environmental" of the lksg framework.
*/
data class LksgEnvironmentalExportImportOfHazardousWasteBaselConvention(
      val persistentOrganicPollutantsProductionAndUseTransboundaryMovements: YesNo? = null,

      val persistentOrganicPollutantsProductionAndUseRiskForImportingState: YesNo? = null,

      val hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: YesNo? = null,

      val hazardousWasteTransboundaryMovementsOutsideOecdEuOrLiechtenstein: YesNo? = null,

      val hazardousWasteDisposal: YesNo? = null,

      val hazardousWasteDisposalRiskOfImport: YesNo? = null,

      val hazardousWasteDisposalOtherWasteImport: YesNo? = null,
)
