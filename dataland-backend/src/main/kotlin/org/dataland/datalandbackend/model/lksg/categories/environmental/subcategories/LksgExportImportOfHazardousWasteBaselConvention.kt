package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Export / Import of hazardous waste (Basel Convention)"
 */
data class LksgExportImportOfHazardousWasteBaselConvention(
    val persistentOrganicPollutantsProductionAndUseTransboundaryMovements: YesNo?,

    val persistentOrganicPollutantsProductionAndUseRiskForImportingState: YesNo?,

    val hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: YesNo?,

    val hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein: YesNo?,

    val hazardousWasteDisposal: YesNo?,

    val hazardousWasteDisposalRiskOfImport: YesNo?,

    val hazardousAndOtherWasteImport: YesNo?,
)
