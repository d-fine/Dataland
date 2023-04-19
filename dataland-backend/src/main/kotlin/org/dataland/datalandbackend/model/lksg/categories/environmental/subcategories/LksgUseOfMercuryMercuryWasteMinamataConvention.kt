package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Use of mercury, mercury waste (Minimata Convention)"
 */
data class LksgUseOfMercuryMercuryWasteMinamataConvention(
    val mercuryAndMercuryWasteHandling: YesNo?,

    val mercuryAndMercuryWasteHandlingPolicy: YesNo?,

    val mercuryAddedProductsHandling: YesNo?,

    val mercuryAddedProductsHandlingRiskOfExposure: YesNo?,

    val mercuryAddedProductsHandlingRiskOfDisposal: YesNo?,

    val mercuryAndMercuryCompoundsProductionAndUse: YesNo?,

    val mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: YesNo?,
)
