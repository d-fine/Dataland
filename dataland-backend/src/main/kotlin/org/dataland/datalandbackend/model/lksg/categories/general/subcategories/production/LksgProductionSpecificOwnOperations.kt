package org.dataland.datalandbackend.model.lksg.categories.general.subcategories.production

import org.dataland.datalandbackend.model.enums.lksg.ProcurementCategory

/**
 * --- API model ---
 * Fields of the LKSG questionnaire regarding the impact topic "Production Specific"
 */
data class LksgProductionSpecificOwnOperations(

    val mostImportantProducts: List<LksgProduct>?,

    val productCategories: Map<ProcurementCategory, LksgProductCategory>?,
)
