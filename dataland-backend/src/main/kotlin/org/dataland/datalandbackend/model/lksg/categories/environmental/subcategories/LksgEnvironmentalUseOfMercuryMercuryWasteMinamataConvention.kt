package org.dataland.datalandbackend.model.lksg.categories.environmental.subcategories

import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.model.BaseDataPoint

/**
 * --- API model ---
 * Fields of the subcategory "Use of mercury, mercury waste (Minamata Convention)" belonging to the category "Environmental" of the lksg framework.
*/
data class LksgEnvironmentalUseOfMercuryMercuryWasteMinamataConvention(
      val mercuryAndMercuryWasteHandling: YesNo? = null,

      val mercuryAndMercuryWasteHandlingPolicy: BaseDataPoint<YesNo>? = null,

      val mercuryAddedProductsHandling: YesNo? = null,

      val mercuryAddedProductsHandlingRiskOfExposure: YesNo? = null,

      val mercuryAddedProductsHandlingRiskOfDisposal: YesNo? = null,

      val mercuryAndMercuryCompoundsProductionAndUse: YesNo? = null,

      val mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: YesNo? = null,
)
