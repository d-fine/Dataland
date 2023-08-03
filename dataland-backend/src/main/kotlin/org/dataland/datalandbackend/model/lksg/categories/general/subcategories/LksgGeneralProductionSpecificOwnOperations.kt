package org.dataland.datalandbackend.model.lksg.categories.general.subcategories

import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production.LksgProduct
import org.dataland.datalandbackend.model.enums.lksg.ProcurementCategoryType
import org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production.LksgProcurementCategory
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackend.utils.JsonExampleFormattingConstants

/**
 * --- API model ---
 * Fields of the subcategory "Production-specific - Own Operations" belonging to the category "General" of the
 * Lksg framework.
*/
data class LksgGeneralProductionSpecificOwnOperations(
      val mostImportantProducts: List<LksgProduct>? = null,

      @field:Schema(example = JsonExampleFormattingConstants.PROCUREMENT_CATEGORIES_DEFAULT_VALUE)
      val productsServicesCategoriesPurchased: Map<ProcurementCategoryType, LksgProcurementCategory>? = null,
)
