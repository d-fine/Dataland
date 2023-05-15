package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Export / Import of hazardous waste (Basel Convention)"
 */
data class LksgExportImportOfHazardousWasteBaselConvention(
        val persistentOrganicPollutantsProductionAndUseTransboundaryMovements: BaseDataPoint<YesNo>?,

        val persistentOrganicPollutantsProductionAndUseRiskForImportingState: BaseDataPoint<YesNo>?,

        val hazardousWasteTransboundaryMovementsLocatedOecdEuLiechtenstein: BaseDataPoint<YesNo>?,

        val hazardousWasteTransboundaryMovementsOutsideOecdEuLiechtenstein: BaseDataPoint<YesNo>?,

        val hazardousWasteDisposal: BaseDataPoint<YesNo>?,

        val hazardousWasteDisposalRiskOfImport: BaseDataPoint<YesNo>?,

        val hazardousAndOtherWasteImport: BaseDataPoint<YesNo>?,
)
