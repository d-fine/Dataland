package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.BaseDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Use of mercury, mercury waste (Minimata Convention)"
 */
data class LksgUseOfMercuryMercuryWasteMinamataConvention(
        val mercuryAndMercuryWasteHandling: BaseDataPoint<YesNo>?,

        val mercuryAndMercuryWasteHandlingPolicy: BaseDataPoint<YesNo>?,

        val mercuryAddedProductsHandling: BaseDataPoint<YesNo>?,

        val mercuryAddedProductsHandlingRiskOfExposure: BaseDataPoint<YesNo>?,

        val mercuryAddedProductsHandlingRiskOfDisposal: BaseDataPoint<YesNo>?,

        val mercuryAndMercuryCompoundsProductionAndUse: BaseDataPoint<YesNo>?,

        val mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: BaseDataPoint<YesNo>?,
)
